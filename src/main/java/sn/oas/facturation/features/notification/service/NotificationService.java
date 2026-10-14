package sn.oas.facturation.features.notification.service;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.notification.dto.NotificationPageResponse;

public interface NotificationService {
    void sendNotification(Client client, String titre, String message);
    void sendInAppNotification(Client client, String titre, String message);
    NotificationPageResponse getClientNotificationPage(Client client, int page, int size);
    void markAsRead(Client client, Long notificationId);
    void markAllAsRead(Client client);
}
