package sn.oas.facturation.features.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.notification.dto.NotificationResponse;
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
            return ResponseEntity.ok(agentNotificationService.getAgentNotifications(agent)
                    .stream()
                    .map(a -> new NotificationResponse(a.id(), a.titre(), a.message(), a.lu(), a.dateCreation()))
                    .toList());
        } else if (user instanceof Client client) {
            return ResponseEntity.ok(notificationService.getClientNotifications(client)
                    .stream()
                    .map(NotificationResponse::from)
                    .toList());
        }

        return ResponseEntity.ok(List.of());
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
