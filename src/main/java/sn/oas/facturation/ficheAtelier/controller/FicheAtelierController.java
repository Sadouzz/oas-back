package sn.oas.facturation.ficheAtelier.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.ficheAtelier.dto.FicheAtelierRequest;
import sn.oas.facturation.ficheAtelier.service.FicheAtelierService;

import java.util.List;

@RestController
@RequestMapping("/api/fiches-atelier")
@RequiredArgsConstructor
public class FicheAtelierController {

    private final FicheAtelierService ficheAtelierService;

    @GetMapping
    public ResponseEntity<List<FicheAtelier>> getAllFichesAtelier() {
        return ResponseEntity.ok(ficheAtelierService.getAllFichesAtelier());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFicheAtelierById(@PathVariable Long id) {
        try {
            FicheAtelier ficheAtelier = ficheAtelierService.getFicheAtelierById(id)
                    .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
            return ResponseEntity.ok(ficheAtelier);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createFicheAtelier(@RequestBody FicheAtelierRequest request) {
        try {
            FicheAtelier ficheAtelier = ficheAtelierService.createFicheAtelier(request);
            return ResponseEntity.ok(ficheAtelier);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFicheAtelier(@PathVariable Long id, @RequestBody FicheAtelierRequest request) {
        try {
            FicheAtelier ficheAtelier = ficheAtelierService.updateFicheAtelier(id, request);
            return ResponseEntity.ok(ficheAtelier);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFicheAtelier(@PathVariable Long id) {
        try {
            ficheAtelierService.deleteFicheAtelier(id);
            return ResponseEntity.ok("Fiche Atelier supprimée avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
