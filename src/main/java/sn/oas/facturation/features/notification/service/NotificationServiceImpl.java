package sn.oas.facturation.features.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.notification.data.entity.Notification;
import sn.oas.facturation.features.notification.repository.NotificationRepository;
import sn.oas.facturation.features.notification.dto.NotificationResponse;
import sn.oas.facturation.features.push.service.PushService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import sn.oas.facturation.features.notification.dto.NotificationPageResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final PushService pushService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    @Override
    public void sendNotification(Client client, String titre, String message) {
        sendNotification(client, titre, message, true);
    }

    @Transactional
    @Override
    public void sendInAppNotification(Client client, String titre, String message) {
        sendNotification(client, titre, message, false);
    }

    private void sendNotification(Client client, String titre, String message, boolean sendEmail) {
        Notification notification = Notification.builder()
                .client(client)
                .titre(titre)
                .message(message)
                .lu(false)
                .build();
        notification = notificationRepository.save(notification);
        NotificationResponse response = NotificationResponse.of(notification);
        runAfterCommit(() -> {
            try {
                pushService.send(client, titre, message);
            } catch (Exception e) {
                log.warn("Erreur push notification client {}: {}", client.getId(), e.getMessage());
            }
            try {
                if (client.getUsername() != null) {
                    messagingTemplate.convertAndSendToUser(client.getUsername(), "/queue/notifications", response);
                }
            } catch (Exception e) {
                log.warn("Erreur lors de l'envoi WebSocket de la notification client {}: {}", client.getId(), e.getMessage());
            }
            if (sendEmail && client.getEmail() != null && !client.getEmail().isEmpty()) {
                try {
                    emailService.sendSimpleEmail(client.getEmail(), titre, message);
                } catch (Exception e) {
                    log.warn("Erreur email notification client {}: {}", client.getId(), e.getMessage());
                }
            }
        });
    }

    private void runAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { action.run(); }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse getClientNotificationPage(Client client, int page, int size) {
        Page<Notification> result = notificationRepository.findByClientIdOrderByDateCreationDescIdDesc(
                client.getId(), PageRequest.of(page, size));
        return new NotificationPageResponse(
                result.getContent().stream().map(NotificationResponse::from).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(),
                notificationRepository.countByClientIdAndLuFalse(client.getId()), result.hasNext());
    }

    @Transactional
    @Override
    public void markAsRead(Client client, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Notification non trouvée avec l'identifiant " + notificationId));
        if (!notification.getClient().getId().equals(client.getId())) {
            throw new sn.oas.facturation.shared.exception.ForbiddenException("Accès non autorisé à cette notification");
        }
        notification.setLu(true);
        notificationRepository.save(notification);
    }

    @Transactional
    @Override
    public void markAllAsRead(Client client) {
        notificationRepository.markAllAsReadForClient(client.getId());
    }
}
