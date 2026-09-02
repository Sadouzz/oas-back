package sn.oas.facturation.features.facture.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.facture.dto.FactureResponse;
import sn.oas.facturation.features.facture.dto.FactureCreateRequest;
import sn.oas.facturation.features.facture.service.FactureService;

import java.util.List;

@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
@Tag(name = "Facture", description = "API pour la gestion des factures finales")
public class FactureController {

    private final FactureService factureService;

    @PostMapping("/creer")
    @Operation(summary = "Créer une nouvelle facture à partir d'une fiche atelier")
    public ResponseEntity<FactureResponse> createFacture(@RequestBody FactureCreateRequest request) {
        return ResponseEntity.ok(factureService.createFacture(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une facture par son ID")
    public ResponseEntity<FactureResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(factureService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Récupérer toutes les factures ou rechercher par mot-clé")
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(factureService.search(keyword));
        }
        return ResponseEntity.ok(factureService.getAll(page, size));
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des factures")
    public ResponseEntity<List<FactureResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(factureService.search(keyword));
    }

    @GetMapping("/recent")
    @Operation(summary = "Récupérer les factures récentes")
    public ResponseEntity<List<FactureResponse>> getRecent() {
        return ResponseEntity.ok(factureService.getRecentFactures());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une facture")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        factureService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Générer le PDF d'une facture")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = factureService.generatePdf(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Facture_" + id + ".pdf");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
    }
}
