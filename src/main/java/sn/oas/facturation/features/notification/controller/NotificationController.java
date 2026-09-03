package sn.oas.facturation.features.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.service.ClientService;
import sn.oas.facturation.features.notification.dto.NotificationResponse;
import sn.oas.facturation.features.notification.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications Client", description = "API pour la gestion des notifications client")
public class NotificationController {

    private final NotificationService notificationService;
    private final ClientService clientService;

    @GetMapping({"", "/me"})
    @Operation(summary = "Lister les notifications du client connecté")
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(notificationService.getClientNotifications(client)
                .stream()
                .map(NotificationResponse::from)
                .toList());
    }

    @PutMapping("/{id}/lu")
    @Operation(summary = "Marquer une notification comme lue")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Long id) {
        Client client = clientService.getClientConnecte();
        notificationService.markAsRead(client, id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/lu-tout")
    @Operation(summary = "Marquer toutes les notifications comme lues")
    public ResponseEntity<Void> markAllNotificationsAsRead() {
        Client client = clientService.getClientConnecte();
        notificationService.markAllAsRead(client);
        return ResponseEntity.ok().build();
    }
}
