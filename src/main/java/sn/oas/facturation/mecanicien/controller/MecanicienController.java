package sn.oas.facturation.mecanicien.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.mecanicien.data.entity.Mecanicien;
import sn.oas.facturation.mecanicien.dto.MecanicienRequest;
import sn.oas.facturation.mecanicien.service.MecanicienService;

import java.util.List;

@RestController
@RequestMapping("/api/mecaniciens")
@RequiredArgsConstructor
public class MecanicienController {

    private final MecanicienService mecanicienService;

    @GetMapping
    public ResponseEntity<List<Mecanicien>> getAllMecaniciens() {
        return ResponseEntity.ok(mecanicienService.getAllMecaniciens());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMecanicienById(@PathVariable Long id) {
        try {
            Mecanicien mecanicien = mecanicienService.getMecanicienById(id)
                    .orElseThrow(() -> new RuntimeException("Mécanicien non trouvé"));
            return ResponseEntity.ok(mecanicien);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createMecanicien(@RequestBody MecanicienRequest request) {
        try {
            Mecanicien mecanicien = mecanicienService.createMecanicien(request);
            return ResponseEntity.ok(mecanicien);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMecanicien(@PathVariable Long id, @RequestBody MecanicienRequest request) {
        try {
            Mecanicien mecanicien = mecanicienService.updateMecanicien(id, request);
            return ResponseEntity.ok(mecanicien);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMecanicien(@PathVariable Long id) {
        try {
            mecanicienService.deleteMecanicien(id);
            return ResponseEntity.ok("Mécanicien supprimé avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
