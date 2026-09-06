package sn.oas.facturation.features.notification.service;

import sn.oas.facturation.features.notification.dto.NotificationResponse;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.user.data.enums.Role;

import java.util.List;

public interface AgentNotificationService {
    void notifyRole(Role role, String titre, String message);
    List<NotificationResponse> getAgentNotifications(Agent agent);
    void markAsRead(Agent agent, Long notificationId);
    void markAllAsRead(Agent agent);
}
