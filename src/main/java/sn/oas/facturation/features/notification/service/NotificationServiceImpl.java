package sn.oas.facturation.features.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.notification.data.entity.Notification;
import sn.oas.facturation.features.notification.dto.NotificationResponse;
import sn.oas.facturation.features.notification.repository.NotificationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    @Override
    public void sendNotification(Client client, String titre, String message) {
        Notification notification = Notification.builder()
                .client(client)
                .titre(titre)
                .message(message)
                .lu(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getClientNotifications(Client client) {
        return notificationRepository.findByClientIdOrderByDateCreationDesc(client.getId());
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
        List<Notification> notifications = notificationRepository.findByClientIdOrderByDateCreationDesc(client.getId());
        for (Notification notification : notifications) {
            if (!notification.isLu()) {
                notification.setLu(true);
            }
        }
        notificationRepository.saveAll(notifications);
    }
}
