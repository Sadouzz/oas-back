package sn.oas.facturation.features.vehicule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.client.service.ClientService;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
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
    public ResponseEntity<Page<Vehicule>> getVehicules(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(vehiculeService.searchVehicules(keyword.trim(), page, size));
        }
        return ResponseEntity.ok(vehiculeService.getAllVehicules(page, size));
    }

    @GetMapping("/recent")
    @Operation(summary = "Récupérer les véhicules récents")
    public ResponseEntity<List<Vehicule>> getRecentVehicules() {
        return ResponseEntity.ok(vehiculeService.getRecentVehicules());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un véhicule par son ID")
    public ResponseEntity<Vehicule> getVehiculeById(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculeService.getVehiculeById(id));
    }

    @PostMapping("/create")
    @Operation(summary = "Créer un nouveau véhicule")
    public ResponseEntity<Vehicule> createVehicule(@RequestBody VehiculeRequest request) {
        return new ResponseEntity<>(vehiculeService.createVehicule(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un véhicule")
    public ResponseEntity<Vehicule> updateVehicule(@PathVariable Long id, @RequestBody VehiculeRequest request) {
        return ResponseEntity.ok(vehiculeService.updateVehicule(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un véhicule")
    public ResponseEntity<Void> deleteVehicule(@PathVariable Long id) {
        vehiculeService.deleteVehicule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Récupérer les véhicules d'un client")
    public ResponseEntity<List<Vehicule>> getVehiculesByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(vehiculeService.getVehiculesByClient(clientId));
    }

    @GetMapping("/me")
    @Operation(summary = "Lister les véhicules du client connecté")
    public ResponseEntity<List<Vehicule>> getMyVehicules() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(vehiculeService.getVehiculesByClient(client.getId()));
    }

    @PostMapping("/me")
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
        return new ResponseEntity<>(vehiculeService.createVehicule(securedRequest), HttpStatus.CREATED);
    }
}
