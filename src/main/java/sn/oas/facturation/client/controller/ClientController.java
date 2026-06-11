package sn.oas.facturation.client.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Clients", description = "API pour la gestion des clients")
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    @Operation(summary = "Lister tous les clients ou rechercher par mot-clé")
    public ResponseEntity<?> listClients(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(clientService.searchClients(keyword));
        }
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/recent")
    @Operation(summary = "Récupérer les clients récents")
    public ResponseEntity<?> getRecentClients() {
        return ResponseEntity.ok(clientService.getRecentClients());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un client par son ID")
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
    @Operation(summary = "Mettre à jour un client")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        try {
            Client client = clientService.updateClient(id, request);
            return ResponseEntity.ok(client);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archiver un client")
    public ResponseEntity<?> archiveClient(@PathVariable Long id) {
        try {
            clientService.archiveClient(id);
            return ResponseEntity.ok("Client archivé avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/unarchive")
    @Operation(summary = "Désarchiver un client")
    public ResponseEntity<?> unarchiveClient(@PathVariable Long id) {
        try {
            clientService.unarchiveClient(id);
            return ResponseEntity.ok("Client désarchivé avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un client")
    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
        try {
            clientService.deleteClient(id);
            return ResponseEntity.ok("Client supprimé avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/anonymize")
    @Operation(summary = "Anonymiser un client pour respecter le RGPD")
    public ResponseEntity<?> anonymizeClient(@PathVariable Long id) {
        try {
            clientService.anonymizeClient(id);
            return ResponseEntity.ok("Client anonymisé avec succès (RGPD) !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
