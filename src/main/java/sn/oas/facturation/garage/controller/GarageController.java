package sn.oas.facturation.garage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.garage.data.entity.Garage;
import sn.oas.facturation.garage.dto.GarageRequest;
import sn.oas.facturation.garage.service.GarageService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/garages")
@RequiredArgsConstructor
public class GarageController {

    private final GarageService garageService;

    @GetMapping
    public ResponseEntity<List<Garage>> getAllGarages() {
        return ResponseEntity.ok(garageService.getAllGarages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGarageById(@PathVariable Long id) {
        try {
            Garage garage = garageService.getGarageById(id)
                    .orElseThrow(() -> new RuntimeException("Garage non trouvé"));
            return ResponseEntity.ok(garage);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createGarage(@RequestBody GarageRequest request) {
        try {
            Garage garage = garageService.createGarage(request);
            return ResponseEntity.ok(garage);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGarage(@PathVariable Long id, @RequestBody GarageRequest request) {
        try {
            Garage garage = garageService.updateGarage(id, request);
            return ResponseEntity.ok(garage);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGarage(@PathVariable Long id) {
        try {
            garageService.deleteGarage(id);
            return ResponseEntity.ok("Garage supprimé avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
