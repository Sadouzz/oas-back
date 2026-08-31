package sn.oas.facturation.noteDePrix.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.noteDePrix.dto.NoteDePrixRequest;
import sn.oas.facturation.noteDePrix.dto.NoteDePrixResponse;
import sn.oas.facturation.noteDePrix.service.NoteDePrixService;

import java.util.List;

@RestController
@RequestMapping({"/api/notes-prix", "/api/notes-de-prix"})
@RequiredArgsConstructor
@Tag(name = "Notes de Prix", description = "API pour la gestion des notes de prix (Factures HT)")
public class NoteDePrixController {

    private final NoteDePrixService noteDePrixService;

    @PostMapping
    @Operation(summary = "Créer une nouvelle note de prix")
    public ResponseEntity<?> createNoteDePrix(@RequestBody NoteDePrixRequest request) {
        try {
            NoteDePrixResponse response = noteDePrixService.createNoteDePrix(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une note de prix")
    public ResponseEntity<?> updateNoteDePrix(@PathVariable Long id, @RequestBody NoteDePrixRequest request) {
        try {
            NoteDePrixResponse response = noteDePrixService.updateNoteDePrix(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une note de prix par son ID")
    public ResponseEntity<?> getNoteDePrixById(@PathVariable Long id) {
        try {
            NoteDePrixResponse response = noteDePrixService.getNoteDePrix(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "Lister toutes les notes de prix")
    public ResponseEntity<List<NoteDePrixResponse>> getAllNotesDePrix() {
        return ResponseEntity.ok(noteDePrixService.getAllNotesDePrix());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une note de prix")
    public ResponseEntity<?> deleteNoteDePrix(@PathVariable Long id) {
        try {
            noteDePrixService.deleteNoteDePrix(id);
            return ResponseEntity.ok("Note de prix supprimée avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
