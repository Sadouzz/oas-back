package sn.oas.facturation.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.auth.data.enums.Role;
import sn.oas.facturation.auth.repository.AgentRepository;
import sn.oas.facturation.notification.data.entity.AgentNotification;
import sn.oas.facturation.notification.dto.AgentNotificationResponse;
import sn.oas.facturation.notification.repository.AgentNotificationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentNotificationServiceImpl implements AgentNotificationService {

    private final AgentNotificationRepository notificationRepository;
    private final AgentRepository agentRepository;

    @Override
    public void notifyRole(Role role, String titre, String message) {
        List<Agent> agents = agentRepository.findByRole(role);
        for (Agent agent : agents) {
            AgentNotification notification = AgentNotification.builder()
                    .agent(agent)
                    .titre(titre)
                    .message(message)
                    .build();
            notificationRepository.save(notification);
        }
    }

    @Override
    public List<AgentNotificationResponse> getAgentNotifications(Agent agent) {
        return notificationRepository.findByAgentIdOrderByDateCreationDesc(agent.getId())
                .stream()
                .map(AgentNotificationResponse::of)
                .toList();
    }

    @Override
    public void markAsRead(Agent agent, Long notificationId) {
        AgentNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée"));

        if (!notification.getAgent().getId().equals(agent.getId())) {
            throw new IllegalArgumentException("Accès non autorisé à cette notification");
        }

        notification.setLu(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(Agent agent) {
        List<AgentNotification> notifications = notificationRepository.findByAgentIdOrderByDateCreationDesc(agent.getId());
        for (AgentNotification notification : notifications) {
            if (!notification.isLu()) {
                notification.setLu(true);
            }
        }
        notificationRepository.saveAll(notifications);
    }
}
