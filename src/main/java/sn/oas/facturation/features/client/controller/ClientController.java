package sn.oas.facturation.features.client.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.dto.ClientCreateRequest;
import sn.oas.facturation.features.client.dto.ClientCreateResponse;
import sn.oas.facturation.features.client.dto.ClientListResponse;
import sn.oas.facturation.features.client.dto.ClientFideleRequest;
import sn.oas.facturation.features.client.dto.ClientUpdateRequest;
import sn.oas.facturation.features.client.service.ClientService;
import sn.oas.facturation.features.client.service.CompteClientService;
import sn.oas.facturation.features.client.dto.CompteClientRequest;
import sn.oas.facturation.features.client.dto.CompteClientResponse;
import sn.oas.facturation.features.client.dto.AlimentationPortefeuilleRequest;
import sn.oas.facturation.features.client.dto.PortefeuilleClientResponse;
import sn.oas.facturation.features.client.dto.RemiseClientRequest;
import sn.oas.facturation.features.auth.service.AuthService;
import sn.oas.facturation.features.client.service.PortefeuilleClientService;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('ROLE_MASTER','MASTER','ROLE_SUPER_AGENT','SUPER_AGENT','ROLE_AGENT','AGENT','ROLE_CHEF_ATELIER','CHEF_ATELIER','ROLE_AGENT_MAGASIN','AGENT_MAGASIN')")
@Tag(name = "Clients", description = "API pour la gestion des clients")
public class ClientController {

    private final ClientService clientService;
    private final AuthService authService;
    private final CompteClientService compteClientService;
    private final PortefeuilleClientService portefeuilleClientService;

    @GetMapping
    @Operation(summary = "Lister tous les clients ou rechercher par mot-clé")
    public ResponseEntity<?> listClients(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(clientService.searchClients(keyword.trim(), page, size).map(ClientListResponse::from));
        }
        return ResponseEntity.ok(clientService.getAllClients(page, size).map(ClientListResponse::from));
    }

    @GetMapping("/archived")
    @Operation(summary = "Lister les clients archivés (paginé et trié par dernier modifié)")
    public ResponseEntity<?> listArchivedClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(clientService.getArchivedClients(page, size).map(ClientListResponse::from));
    }

    @GetMapping("/me")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('ROLE_CLIENT','CLIENT')")
    @Operation(summary = "Récupérer le profil du client connecté")
    public ResponseEntity<ClientListResponse> getProfile() {
        return ResponseEntity.ok(ClientListResponse.from(clientService.getClientConnecte()));
    }

    @GetMapping("/recent")
    @Operation(summary = "Récupérer les clients récents")
    public ResponseEntity<?> getRecentClients() {
        return ResponseEntity.ok(clientService.getRecentClients().stream().map(ClientListResponse::from).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un client par son ID")
    public ResponseEntity<?> getClientById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ClientListResponse.from(clientService.getClientById(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping({"", "/create"})
    @Operation(summary = "Créer un nouveau client")
    public ResponseEntity<ClientCreateResponse> createClient(@RequestBody @Valid ClientCreateRequest request) {
        ClientCreateResponse response = clientService.createClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('ROLE_MASTER','MASTER','ROLE_SUPER_AGENT','SUPER_AGENT','ROLE_AGENT','AGENT','ROLE_CHEF_ATELIER','CHEF_ATELIER','ROLE_AGENT_MAGASIN','AGENT_MAGASIN','ROLE_CLIENT','CLIENT')")
    @Operation(summary = "Mettre à jour un client")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @RequestBody @Valid ClientUpdateRequest request) {
        boolean clientConnecte = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT") || a.getAuthority().equals("CLIENT"));
        if (clientConnecte && !id.equals(clientService.getClientConnecte().getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Un client ne peut modifier que son propre profil.");
        }
        try {
            ClientUpdateRequest update = clientConnecte
                    ? new ClientUpdateRequest(request.phone(), request.firstName(), request.lastName(), request.email(),
                            null, null, null, null, null, null)
                    : request;
            Client client = clientService.updateClient(id, update);
            return ResponseEntity.ok(ClientListResponse.from(client));
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

    @PatchMapping("/{id}/fidele")
    @Operation(summary = "Passer un client en fidèle ou mettre à jour ses paramètres")
    public ResponseEntity<?> updateClientFideleConfig(@PathVariable Long id, @RequestBody ClientFideleRequest request) {
        clientService.updateClientFideleConfig(id, request);
        return ResponseEntity.ok("Configuration du client fidèle mise à jour avec succès !");
    }

    @DeleteMapping("/{id}/fidele")
    @Operation(summary = "Retirer le statut de client fidèle")
    public ResponseEntity<?> removeClientFidele(@PathVariable Long id) {
        clientService.removeClientFidele(id);
        return ResponseEntity.ok("Statut client fidèle retiré avec succès !");
    }

    @PutMapping("/{id}/remise")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('ROLE_MASTER','MASTER','ROLE_SUPER_AGENT','SUPER_AGENT')")
    @Operation(summary = "Configurer la remise commerciale d'un client")
    public ResponseEntity<ClientListResponse> configurerRemise(@PathVariable Long id,
            @RequestBody @Valid RemiseClientRequest request) {
        return ResponseEntity.ok(ClientListResponse.from(clientService.configurerRemise(id, request.montantRemise())));
    }

    @GetMapping("/{id}/portefeuille-prepaye")
    @Operation(summary = "Consulter le solde prépayé et son historique")
    public ResponseEntity<PortefeuilleClientResponse> lirePortefeuille(@PathVariable Long id) {
        return ResponseEntity.ok(portefeuilleClientService.lire(id));
    }

    @PostMapping("/{id}/portefeuille-prepaye/alimentations")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('ROLE_MASTER','MASTER','ROLE_SUPER_AGENT','SUPER_AGENT','ROLE_AGENT','AGENT','ROLE_CHEF_ATELIER','CHEF_ATELIER','ROLE_AGENT_MAGASIN','AGENT_MAGASIN')")
    @Operation(summary = "Enregistrer un versement sur le solde prépayé d'un client")
    public ResponseEntity<PortefeuilleClientResponse> alimenterPortefeuille(@PathVariable Long id,
            @RequestBody @Valid AlimentationPortefeuilleRequest request) {
        return ResponseEntity.ok(portefeuilleClientService.alimenter(id, request));
    }

    @GetMapping("/{id}/compte-financier")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('ROLE_MASTER','MASTER','ROLE_SUPER_AGENT','SUPER_AGENT')")
    @Operation(summary = "Consulter les conditions financières d'un client")
    public ResponseEntity<CompteClientResponse> getCompteFinancier(@PathVariable Long id) {
        return ResponseEntity.ok(compteClientService.lire(id));
    }

    @PutMapping("/{id}/compte-financier")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('ROLE_MASTER','MASTER','ROLE_SUPER_AGENT','SUPER_AGENT')")
    @Operation(summary = "Ouvrir ou mettre à jour le compte financier d'une entreprise")
    public ResponseEntity<CompteClientResponse> configurerCompteFinancier(@PathVariable Long id, @RequestBody @Valid CompteClientRequest request) {
        return ResponseEntity.ok(compteClientService.configurer(id, request));
    }

    @DeleteMapping("/{id}/compte-financier")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('ROLE_MASTER','MASTER','ROLE_SUPER_AGENT','SUPER_AGENT')")
    @Operation(summary = "Désactiver le compte sans effacer l'historique financier")
    public ResponseEntity<Void> desactiverCompteFinancier(@PathVariable Long id) {
        compteClientService.desactiver(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/conditions-financieres")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('ROLE_MASTER','MASTER','ROLE_SUPER_AGENT','SUPER_AGENT')")
    @Operation(summary = "Définir le plafond et l'échéance applicables à tout type de client")
    public ResponseEntity<ClientListResponse> updateConditionsFinancieres(@PathVariable Long id,
            @RequestBody @Valid sn.oas.facturation.features.client.dto.ConditionsFinancieresRequest request) {
        return ResponseEntity.ok(ClientListResponse.from(clientService.updateConditionsFinancieres(id, request)));
    }
}
