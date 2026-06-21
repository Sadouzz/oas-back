package sn.oas.facturation.notification.service;

import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.auth.data.enums.Role;
import sn.oas.facturation.notification.dto.AgentNotificationResponse;

import java.util.List;

public interface AgentNotificationService {
    void notifyRole(Role role, String titre, String message);
    List<AgentNotificationResponse> getAgentNotifications(Agent agent);
    void markAsRead(Agent agent, Long notificationId);
    void markAllAsRead(Agent agent);
}
