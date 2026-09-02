package sn.oas.facturation.features.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import sn.oas.facturation.features.auth.data.entity.Agent;
import sn.oas.facturation.features.auth.data.enums.Role;
import sn.oas.facturation.features.auth.repository.AgentRepository;
import sn.oas.facturation.features.notification.data.entity.AgentNotification;
import sn.oas.facturation.features.notification.dto.AgentNotificationResponse;
import sn.oas.facturation.features.notification.repository.AgentNotificationRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentNotificationServiceImpl implements AgentNotificationService {

    private final AgentNotificationRepository notificationRepository;
    private final AgentRepository agentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void notifyRole(Role role, String titre, String message) {
        List<Agent> agents = agentRepository.findByRole(role);
        for (Agent agent : agents) {
            AgentNotification notification = AgentNotification.builder()
                    .agent(agent)
                    .titre(titre)
                    .message(message)
                    .build();
            notification = notificationRepository.save(notification);

            AgentNotificationResponse responseDto = AgentNotificationResponse.of(notification);

            try {
                // 1. Diffusion sur le topic du rôle (/topic/roles/AGENT/notifications)
                messagingTemplate.convertAndSend("/topic/roles/" + role.name() + "/notifications", responseDto);

                // 2. Diffusion ciblée sur l'agent spécifique (/topic/agent/12/notifications)
                messagingTemplate.convertAndSend("/topic/agent/" + agent.getId() + "/notifications", responseDto);

                // 3. Diffusion utilisateur STOMP (/user/{username}/queue/notifications)
                if (agent.getUsername() != null) {
                    messagingTemplate.convertAndSendToUser(agent.getUsername(), "/queue/notifications", responseDto);
                }
            } catch (Exception e) {
                log.warn("Erreur lors de l'envoi de la notification WebSocket pour l'agent {}: {}", agent.getUsername(), e.getMessage());
            }
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
