package sn.oas.facturation.features.noteDePrix.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import sn.oas.facturation.features.noteDePrix.dto.NoteDePrixListResponse;
import sn.oas.facturation.features.noteDePrix.dto.NoteDePrixRequest;
import sn.oas.facturation.features.noteDePrix.dto.NoteDePrixResponse;
import sn.oas.facturation.features.noteDePrix.service.NoteDePrixService;


@RestController
@RequestMapping({"/api/notes-prix", "/api/notes-de-prix"})
@RequiredArgsConstructor
@Tag(name = "Notes de Prix", description = "API pour la gestion des notes de prix (Factures HT)")
public class NoteDePrixController {

    private final NoteDePrixService noteDePrixService;

    @PostMapping
    @Operation(summary = "Créer une nouvelle note de prix")
    public ResponseEntity<NoteDePrixResponse> createNoteDePrix(@RequestBody NoteDePrixRequest request) {
        return ResponseEntity.ok(NoteDePrixResponse.from(noteDePrixService.createNoteDePrix(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une note de prix")
    public ResponseEntity<NoteDePrixResponse> updateNoteDePrix(@PathVariable Long id, @RequestBody NoteDePrixRequest request) {
        return ResponseEntity.ok(NoteDePrixResponse.from(noteDePrixService.updateNoteDePrix(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une note de prix par son ID")
    public ResponseEntity<NoteDePrixResponse> getNoteDePrixById(@PathVariable Long id) {
        return ResponseEntity.ok(NoteDePrixResponse.from(noteDePrixService.getNoteDePrix(id)));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les notes de prix ou rechercher avec filtres et pagination")
    public ResponseEntity<Page<NoteDePrixListResponse>> getAllNotesDePrix(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) String immatriculation,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String clientNumero,
            @RequestParam(required = false) String clientMatricule,
            @RequestParam(required = false) String client,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Long ordreReparationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(noteDePrixService.getAllFiltered(
                keyword, numero, immatriculation, clientId, clientNumero, clientMatricule, client,
                statut, dateDebut, dateFin, ordreReparationId, page, size)
                .map(NoteDePrixListResponse::from));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une note de prix")
    public ResponseEntity<Void> deleteNoteDePrix(@PathVariable Long id) {
        noteDePrixService.deleteNoteDePrix(id);
        return ResponseEntity.noContent().build();
    }
}
