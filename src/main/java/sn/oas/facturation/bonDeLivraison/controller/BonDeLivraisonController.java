package sn.oas.facturation.bonDeLivraison.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.bonDeLivraison.dto.BonDeLivraisonCreateRequest;
import sn.oas.facturation.bonDeLivraison.dto.BonDeLivraisonResponse;
import sn.oas.facturation.bonDeLivraison.dto.BonDeLivraisonUpdateRequest;
import sn.oas.facturation.bonDeLivraison.service.BonDeLivraisonService;

import java.util.List;

@RestController
@RequestMapping("/api/bons-de-livraison")
@RequiredArgsConstructor
@Tag(name = "Bon de Livraison", description = "API pour la gestion des bons de livraison")
public class BonDeLivraisonController {

    private final BonDeLivraisonService bonDeLivraisonService;

    @PostMapping
    @Operation(summary = "Créer un bon de livraison")
    public ResponseEntity<BonDeLivraisonResponse> create(@Valid @RequestBody BonDeLivraisonCreateRequest request) {
        return new ResponseEntity<>(bonDeLivraisonService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un bon de livraison")
    public ResponseEntity<BonDeLivraisonResponse> update(@PathVariable Long id, @Valid @RequestBody BonDeLivraisonUpdateRequest request) {
        return ResponseEntity.ok(bonDeLivraisonService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un bon de livraison par son ID")
    public ResponseEntity<BonDeLivraisonResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bonDeLivraisonService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les bons de livraison")
    public ResponseEntity<List<BonDeLivraisonResponse>> getAll() {
        return ResponseEntity.ok(bonDeLivraisonService.getAll());
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des bons de livraison")
    public ResponseEntity<List<BonDeLivraisonResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(bonDeLivraisonService.search(keyword));
    }

    @GetMapping("/recent")
    @Operation(summary = "Récupérer les bons de livraison récents")
    public ResponseEntity<List<BonDeLivraisonResponse>> getRecent() {
        return ResponseEntity.ok(bonDeLivraisonService.getRecentBonsDeLivraison());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un bon de livraison")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bonDeLivraisonService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Générer le PDF d'un bon de livraison")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = bonDeLivraisonService.generatePdf(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "BL_" + id + ".pdf");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
    }
}
