package sn.oas.facturation.features.connectionHistory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import sn.oas.facturation.features.connectionHistory.dto.ConnectionHistoryResponse;
import sn.oas.facturation.features.connectionHistory.service.ConnectionHistoryService;

import java.util.List;

@RestController
@RequestMapping("/api/connection-history")
@RequiredArgsConstructor
@Tag(name = "Historique de connexion", description = "API pour consulter l'historique des connexions")
public class ConnectionHistoryController {

    private final ConnectionHistoryService connectionHistoryService;

    @GetMapping
    @Operation(summary = "Lister l'historique des connexions ou rechercher par mot-clé avec pagination")
    public ResponseEntity<Page<ConnectionHistoryResponse>> getConnectionHistory(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(connectionHistoryService.searchConnectionHistory(keyword.trim(), page, size)
                    .map(ConnectionHistoryResponse::from));
        }
        return ResponseEntity.ok(connectionHistoryService.getAllConnectionHistory(page, size)
                .map(ConnectionHistoryResponse::from));
    }

    @GetMapping("/all")
    @Operation(summary = "Récupérer l'historique de toutes les connexions sans pagination")
    public ResponseEntity<List<ConnectionHistoryResponse>> getAllConnectionHistory() {
        return ResponseEntity.ok(connectionHistoryService.getAllConnectionHistory().stream()
                .map(ConnectionHistoryResponse::from).toList());
    }
}
