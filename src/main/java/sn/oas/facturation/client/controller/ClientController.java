package sn.oas.facturation.client.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.auth.dto.RegisterRequest;
import sn.oas.facturation.auth.dto.UserUpdateRequest;
import sn.oas.facturation.client.service.ClientService;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<?> listClients(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(clientService.searchClients(keyword));
        }
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/recent")
    public ResponseEntity<?> getRecentClients() {
        return ResponseEntity.ok(clientService.getRecentClients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getClientById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(clientService.getClientById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /*@PostMapping("/create")
    public ResponseEntity<?> createClient(@RequestBody RegisterRequest request) {
        try {
            Client client = clientService.createClient(request);
            return ResponseEntity.ok(client);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }*/

    @PutMapping("/{id}")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        try {
            Client client = clientService.updateClient(id, request);
            return ResponseEntity.ok(client);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<?> archiveClient(@PathVariable Long id) {
        try {
            clientService.archiveClient(id);
            return ResponseEntity.ok("Client archivé avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/unarchive")
    public ResponseEntity<?> unarchiveClient(@PathVariable Long id) {
        try {
            clientService.unarchiveClient(id);
            return ResponseEntity.ok("Client désarchivé avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
        try {
            clientService.deleteClient(id);
            return ResponseEntity.ok("Client supprimé avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
