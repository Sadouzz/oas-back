package sn.oas.facturation.notification.service;

import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {
    void sendNotification(Client client, String titre, String message);
    List<NotificationResponse> getClientNotifications(Client client);
    void markAsRead(Client client, Long notificationId);
    void markAllAsRead(Client client);
}
