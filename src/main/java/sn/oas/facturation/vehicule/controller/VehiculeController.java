package sn.oas.facturation.vehicule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import sn.oas.facturation.vehicule.dto.VehiculeRequest;
import sn.oas.facturation.vehicule.service.VehiculeService;

import java.util.List;

@RestController
@RequestMapping("/api/vehicules")
@RequiredArgsConstructor
public class VehiculeController {

    private final VehiculeService vehiculeService;

    @GetMapping
    public ResponseEntity<List<Vehicule>> getVehicules(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(vehiculeService.searchVehicules(keyword));
        }
        return ResponseEntity.ok(vehiculeService.getAllVehicules());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Vehicule>> getRecentVehicules() {
        return ResponseEntity.ok(vehiculeService.getRecentVehicules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVehiculeById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(vehiculeService.getVehiculeById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createVehicule(@RequestBody VehiculeRequest request) {
        try {
            Vehicule vehicule = vehiculeService.createVehicule(request);
            return ResponseEntity.ok(vehicule);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicule(@PathVariable Long id, @RequestBody VehiculeRequest request) {
        try {
            Vehicule vehicule = vehiculeService.updateVehicule(id, request);
            return ResponseEntity.ok(vehicule);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicule(@PathVariable Long id) {
        try {
            vehiculeService.deleteVehicule(id);
            return ResponseEntity.ok("Véhicule supprimé avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Vehicule>> getVehiculesByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(vehiculeService.getVehiculesByClient(clientId));
    }
}
