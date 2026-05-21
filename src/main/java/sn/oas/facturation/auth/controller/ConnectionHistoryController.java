package sn.oas.facturation.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.oas.facturation.auth.data.entity.ConnectionHistory;
import sn.oas.facturation.auth.service.ConnectionHistoryService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/connection-history")
@RequiredArgsConstructor
public class ConnectionHistoryController {

    private final ConnectionHistoryService connectionHistoryService;

    @GetMapping
    public ResponseEntity<List<ConnectionHistory>> getConnectionHistory() {
        return ResponseEntity.ok(connectionHistoryService.getAllConnectionHistory());
    }
}
