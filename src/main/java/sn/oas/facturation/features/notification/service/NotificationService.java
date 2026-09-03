package sn.oas.facturation.features.notification.service;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.notification.data.entity.Notification;

import java.util.List;

public interface NotificationService {
    void sendNotification(Client client, String titre, String message);
    List<Notification> getClientNotifications(Client client);
    void markAsRead(Client client, Long notificationId);
    void markAllAsRead(Client client);
}
