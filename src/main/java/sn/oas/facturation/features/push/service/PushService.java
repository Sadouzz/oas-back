package sn.oas.facturation.features.push.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.push.data.entity.PushSubscription;
import sn.oas.facturation.features.push.repository.PushSubscriptionRepository;
import sn.oas.facturation.features.user.data.entity.User;

import java.net.URI;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushService {
    private final PushSubscriptionRepository subscriptions;
    @Value("${app.push.vapid-public-key:}") private String publicKey;
    @Value("${app.push.vapid-private-key:}") private String privateKey;
    @Value("${app.push.subject:mailto:admin@orientautoservice.sn}") private String subject;

    public String publicKey() { return publicKey; }

    @Transactional
    public void upsert(User user, String endpoint, String p256dh, String auth) {
        validateEndpoint(endpoint);
        PushSubscription subscription = subscriptions.findByEndpoint(endpoint).orElseGet(PushSubscription::new);
        subscription.setUser(user);
        subscription.setEndpoint(endpoint);
        subscription.setPublicKey(p256dh);
        subscription.setAuthSecret(auth);
        subscriptions.save(subscription);
    }

    @Transactional
    public void remove(User user, String endpoint) { subscriptions.deleteByEndpointAndUserId(endpoint, user.getId()); }

    @Async("pushExecutor")
    @Transactional(readOnly = true)
    public void send(User user, String title, String message) {
        if (publicKey == null || publicKey.isBlank() || privateKey == null || privateKey.isBlank()) return;
        List<PushSubscription> registered = subscriptions.findByUserId(user.getId());
        if (registered.isEmpty()) return;
        try {
            nl.martijndwars.webpush.PushService client = new nl.martijndwars.webpush.PushService(publicKey, privateKey, subject);
            String payload = "{\"notification\":{\"title\":\"" + escape(title) + "\",\"body\":\"" + escape(message)
                    + "\",\"icon\":\"/oas-logo.png\",\"data\":{\"onActionClick\":{\"default\":{\"operation\":\"openWindow\",\"url\":\"/\"}}}}}";
            for (PushSubscription subscription : registered) {
                try {
                    client.send(new Notification(subscription.getEndpoint(), subscription.getPublicKey(), subscription.getAuthSecret(), payload));
                } catch (Exception e) {
                    log.warn("Échec d'envoi push pour l'utilisateur {}: {}", user.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Service push indisponible : {}", e.getMessage());
        }
    }

    private void validateEndpoint(String endpoint) {
        try {
            URI uri = URI.create(endpoint);
            String host = uri.getHost();
            boolean allowed = "https".equalsIgnoreCase(uri.getScheme()) && host != null && (
                    host.equals("fcm.googleapis.com") || host.endsWith(".push.services.mozilla.com")
                    || host.equals("push.services.mozilla.com") || host.equals("web.push.apple.com")
                    || host.equals("notify.windows.com") || host.endsWith(".notify.windows.com"));
            if (!allowed) throw new IllegalArgumentException("Endpoint push non pris en charge.");
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Endpoint push invalide ou non pris en charge.");
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
