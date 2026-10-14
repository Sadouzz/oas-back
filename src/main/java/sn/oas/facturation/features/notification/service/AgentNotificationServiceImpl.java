package sn.oas.facturation.features.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.stereotype.Service;

import sn.oas.facturation.features.notification.data.entity.AgentNotification;
import sn.oas.facturation.features.notification.dto.NotificationResponse;
import sn.oas.facturation.features.notification.repository.AgentNotificationRepository;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.user.data.enums.Role;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.user.repository.AgentRepository;
import sn.oas.facturation.features.push.service.PushService;
import sn.oas.facturation.shared.exception.ForbiddenException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import sn.oas.facturation.features.notification.dto.NotificationPageResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentNotificationServiceImpl implements AgentNotificationService {

    private final AgentNotificationRepository notificationRepository;
    private final AgentRepository agentRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PushService pushService;

    @Override
    @Transactional
    public void notifyRole(Role role, String titre, String message) {
        notifyRoleForAgents(agentRepository.findNotificationRecipientsByRole(role.name()), titre, message);
    }

    @Override
    @Transactional
    public void notifyRoleInGarage(Role role, Garage garage, String titre, String message) {
        if (garage == null || garage.getId() == null) {
            throw new IllegalArgumentException("Une notification ciblée doit préciser un garage enregistré.");
        }
        notifyRoleForAgents(agentRepository.findNotificationRecipientsByRoleAndGarage(role.name(), garage.getId()),
                titre, message);
    }

    private void notifyRoleForAgents(List<Agent> agents, String titre, String message) {
        for (Agent agent : agents) {
            AgentNotification notification = AgentNotification.builder()
                    .agent(agent)
                    .titre(titre)
                    .message(message)
                    .build();
            notification = notificationRepository.save(notification);
            NotificationResponse responseDto = NotificationResponse.of(notification);
            publishAfterCommit(agent, responseDto, titre, message);
        }
    }

    private void publishAfterCommit(Agent agent, NotificationResponse response, String title, String message) {
        Runnable publish = () -> {
            try {
                pushService.send(agent, title, message);
            } catch (Exception e) {
                log.warn("Erreur push notification agent {}: {}", agent.getId(), e.getMessage());
            }
            try {
                if (agent.getUsername() != null) {
                    messagingTemplate.convertAndSendToUser(agent.getUsername(), "/queue/notifications", response);
                }
            } catch (Exception e) {
                log.warn("Erreur lors de l'envoi WebSocket de la notification pour l'agent {}: {}",
                        agent.getUsername(), e.getMessage());
            }
        };
        runAfterCommit(publish);
    }

    private void runAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse getAgentNotificationPage(Agent agent, int page, int size) {
        Page<AgentNotification> result = notificationRepository.findByAgentIdOrderByDateCreationDescIdDesc(
                agent.getId(), PageRequest.of(page, size));
        return new NotificationPageResponse(
                result.getContent().stream().map(NotificationResponse::of).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(),
                notificationRepository.countByAgentIdAndLuFalse(agent.getId()), result.hasNext());
    }

    @Override
    @Transactional
    public void markAsRead(Agent agent, Long notificationId) {
        AgentNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification non trouvée avec l'identifiant " + notificationId));

        if (!notification.getAgent().getId().equals(agent.getId())) {
            throw new ForbiddenException("Accès non autorisé à cette notification");
        }

        notification.setLu(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Agent agent) {
        notificationRepository.markAllAsReadForAgent(agent.getId());
    }
}
