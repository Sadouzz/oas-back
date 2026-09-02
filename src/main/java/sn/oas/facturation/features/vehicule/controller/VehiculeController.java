package sn.oas.facturation.features.vehicule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping
    @Operation(summary = "Lister tous les véhicules ou rechercher par mot-clé")
    public ResponseEntity<?> getVehicules(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(vehiculeService.searchVehicules(keyword));
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
    public ResponseEntity<?> getVehiculeById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(vehiculeService.getVehiculeById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/create")
    @Operation(summary = "Créer un nouveau véhicule")
    public ResponseEntity<?> createVehicule(@RequestBody VehiculeRequest request) {
        try {
            Vehicule vehicule = vehiculeService.createVehicule(request);
            return ResponseEntity.ok(vehicule);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un véhicule")
    public ResponseEntity<?> updateVehicule(@PathVariable Long id, @RequestBody VehiculeRequest request) {
        try {
            Vehicule vehicule = vehiculeService.updateVehicule(id, request);
            return ResponseEntity.ok(vehicule);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un véhicule")
    public ResponseEntity<?> deleteVehicule(@PathVariable Long id) {
        try {
            vehiculeService.deleteVehicule(id);
            return ResponseEntity.ok("{\"message\": \"Véhicule supprimé avec succès !\"}");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Récupérer les véhicules d'un client")
    public ResponseEntity<List<Vehicule>> getVehiculesByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(vehiculeService.getVehiculesByClient(clientId));
    }
}
