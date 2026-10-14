package sn.oas.facturation.features.notification.service;

import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.user.data.enums.Role;
import sn.oas.facturation.features.garage.data.entity.Garage;

import sn.oas.facturation.features.notification.dto.NotificationPageResponse;

public interface AgentNotificationService {
    void notifyRole(Role role, String titre, String message);
    void notifyRoleInGarage(Role role, Garage garage, String titre, String message);
    NotificationPageResponse getAgentNotificationPage(Agent agent, int page, int size);
    void markAsRead(Agent agent, Long notificationId);
    void markAllAsRead(Agent agent);
}
