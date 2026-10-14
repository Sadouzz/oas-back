package sn.oas.facturation.features.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.notification.dto.NotificationResponse;
import sn.oas.facturation.features.notification.dto.NotificationPageResponse;
import sn.oas.facturation.features.notification.service.AgentNotificationService;
import sn.oas.facturation.features.notification.service.NotificationService;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.user.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "API unifiée pour la gestion des notifications (Agents et Clients)")
public class NotificationController {

    private final NotificationService notificationService;
    private final AgentNotificationService agentNotificationService;
    private final UserRepository userRepository;

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) return null;
        return userRepository.findByUsername(authentication.getName())
                .or(() -> userRepository.findByEmail(authentication.getName()))
                .orElse(null);
    }

    @GetMapping({"", "/me"})
    @Operation(summary = "Lister les notifications de l'utilisateur connecté (Agent ou Client)")
    public ResponseEntity<List<NotificationResponse>> getNotifications(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        if (user instanceof Agent agent) {
            return ResponseEntity.ok(agentNotificationService.getAgentNotificationPage(agent, 0, 100).content());
        } else if (user instanceof Client client) {
            return ResponseEntity.ok(notificationService.getClientNotificationPage(client, 0, 100).content());
        }

        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/page")
    @Operation(summary = "Lister une page de notifications avec le nombre total de non lues")
    public ResponseEntity<NotificationPageResponse> getNotificationPage(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new sn.oas.facturation.shared.exception.BadRequestException("La page ou la taille demandée est invalide.");
        }
        User user = getAuthenticatedUser(authentication);
        if (user == null) return ResponseEntity.status(401).build();
        if (user instanceof Agent agent) {
            return ResponseEntity.ok(agentNotificationService.getAgentNotificationPage(agent, page, size));
        }
        if (user instanceof Client client) {
            return ResponseEntity.ok(notificationService.getClientNotificationPage(client, page, size));
        }
        return ResponseEntity.ok(new NotificationPageResponse(List.of(), page, size, 0, 0, false));
    }

    @RequestMapping(value = {"/{id}/read", "/{id}/lu"}, method = {RequestMethod.POST, RequestMethod.PUT})
    @Operation(summary = "Marquer une notification comme lue")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        if (user instanceof Agent agent) {
            agentNotificationService.markAsRead(agent, id);
        } else if (user instanceof Client client) {
            notificationService.markAsRead(client, id);
        }

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = {"/read-all", "/lu-tout"}, method = {RequestMethod.POST, RequestMethod.PUT})
    @Operation(summary = "Marquer toutes les notifications comme lues")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        if (user instanceof Agent agent) {
            agentNotificationService.markAllAsRead(agent);
        } else if (user instanceof Client client) {
            notificationService.markAllAsRead(client);
        }

        return ResponseEntity.ok().build();
    }
}
