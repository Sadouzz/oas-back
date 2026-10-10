package sn.oas.facturation.features.vehicule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.service.ClientService;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.dto.VehiculeListResponse;
import sn.oas.facturation.features.vehicule.dto.VehiculeRequest;
import sn.oas.facturation.features.vehicule.service.VehiculeService;

import java.util.List;

@RestController
@RequestMapping("/api/vehicules")
@RequiredArgsConstructor
@Tag(name = "Véhicules", description = "API pour la gestion des véhicules")
public class VehiculeController {

    private final VehiculeService vehiculeService;
    private final ClientService clientService;

    @GetMapping
    @Operation(summary = "Lister tous les véhicules ou rechercher par mot-clé avec pagination")
    public ResponseEntity<Page<VehiculeListResponse>> getVehicules(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity
                    .ok(vehiculeService.searchVehicules(keyword.trim(), page, size).map(VehiculeListResponse::from));
        }
        return ResponseEntity.ok(vehiculeService.getAllVehicules(page, size).map(VehiculeListResponse::from));
    }

    @GetMapping("/recent")
    @Operation(summary = "Récupérer les véhicules récents")
    public ResponseEntity<List<VehiculeListResponse>> getRecentVehicules() {
        return ResponseEntity
                .ok(vehiculeService.getRecentVehicules().stream().map(VehiculeListResponse::from).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un véhicule par son ID")
    public ResponseEntity<Vehicule> getVehiculeById(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculeService.getVehiculeById(id));
    }

    @PostMapping({"", "/create"})
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
    @Operation(summary = "Créer un nouveau véhicule")
    /*@Caching(evict = {
        @CacheEvict(value = "dashboard_super_agent", allEntries = true),
        @CacheEvict(value = "dashboard_chef_atelier", allEntries = true),
        @CacheEvict(value = "dashboard_agent", allEntries = true)
    })*/
    public ResponseEntity<Vehicule> createVehicule(@RequestBody VehiculeRequest request) {
        return new ResponseEntity<>(vehiculeService.createVehicule(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER')")
    @Operation(summary = "Mettre à jour un véhicule")
    public ResponseEntity<Vehicule> updateVehicule(@PathVariable Long id, @RequestBody VehiculeRequest request) {
        return ResponseEntity.ok(vehiculeService.updateVehicule(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER')")
    @Operation(summary = "Supprimer un véhicule")
/*@Caching(evict = {
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_chef_atelier", allEntries = true),
            @CacheEvict(value = "dashboard_agent", allEntries = true)
    })*/
    public ResponseEntity<Void> deleteVehicule(@PathVariable Long id) {
        vehiculeService.deleteVehicule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Récupérer les véhicules d'un client, avec pagination optionnelle")
    public ResponseEntity<?> getVehiculesByClient(@PathVariable Long clientId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null || size != null) {
            int pageIndex = page == null ? 0 : page;
            int pageSize = size == null ? 10 : size;
            return ResponseEntity.ok(vehiculeService.getVehiculesByClient(clientId, pageIndex, pageSize)
                    .map(VehiculeListResponse::from));
        }
        return ResponseEntity.ok(vehiculeService.getVehiculesByClient(clientId).stream()
                .map(VehiculeListResponse::from).toList());
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ROLE_CLIENT')")
    @Operation(summary = "Lister les véhicules du client connecté")
    public ResponseEntity<List<VehiculeListResponse>> getMyVehicules() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(
                vehiculeService.getVehiculesActifsByClient(client.getId()).stream().map(VehiculeListResponse::from).toList());
    }

    @PostMapping("/me")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ROLE_CLIENT')")
    @Operation(summary = "Enregistrer un véhicule pour le client connecté")
    public ResponseEntity<Vehicule> addMyVehicule(@RequestBody VehiculeRequest request) {
        Client client = clientService.getClientConnecte();
        VehiculeRequest securedRequest = new VehiculeRequest(
                request.immatriculation(),
                request.annee(),
                request.modele(),
                request.marque(),
                request.kilometrage(),
                request.numeroChassis(),
                client.getId());
        return new ResponseEntity<>(vehiculeService.createVehicule(securedRequest, false), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/activer")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER')")
    @Operation(summary = "Activer un véhicule créé par un client")
    public ResponseEntity<VehiculeListResponse> activerVehicule(@PathVariable Long id) {
        return ResponseEntity.ok(VehiculeListResponse.from(vehiculeService.activerVehicule(id)));
    }

    @DeleteMapping("/me/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ROLE_CLIENT')")
    @Operation(summary = "Archiver un véhicule du client connecté")
    public ResponseEntity<Void> archiveMyVehicule(@PathVariable Long id) {
        Client client = clientService.getClientConnecte();
        vehiculeService.archiveVehiculeByClient(id, client.getId());
        return ResponseEntity.noContent().build();
    }
}
