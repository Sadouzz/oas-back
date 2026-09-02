package sn.oas.facturation.features.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.oas.facturation.features.auth.data.entity.ConnectionHistory;
import sn.oas.facturation.features.auth.service.ConnectionHistoryService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/connection-history")
@RequiredArgsConstructor
@Tag(name = "Historique de connexion", description = "API pour consulter l'historique des connexions")
public class ConnectionHistoryController {

    private final ConnectionHistoryService connectionHistoryService;

    @GetMapping
    @Operation(summary = "Récupérer l'historique de toutes les connexions")
    public ResponseEntity<List<ConnectionHistory>> getConnectionHistory() {
        return ResponseEntity.ok(connectionHistoryService.getAllConnectionHistory());
    }
}
