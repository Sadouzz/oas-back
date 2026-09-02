package sn.oas.facturation.features.bonDeReception.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.bonDeReception.dto.BonDeReceptionCreateRequest;
import sn.oas.facturation.features.bonDeReception.dto.BonDeReceptionResponse;
import sn.oas.facturation.features.bonDeReception.dto.BonDeReceptionUpdateRequest;
import sn.oas.facturation.features.bonDeReception.service.BonDeReceptionService;

import java.util.List;

@RestController
@RequestMapping({"/api/bons-de-reception", "/api/bons-de-livraison"})
@RequiredArgsConstructor
@Tag(name = "Bon de Réception", description = "API pour la gestion des bons de réception")
public class BonDeReceptionController {

    private final BonDeReceptionService bonDeReceptionService;

    @PostMapping
    @Operation(summary = "Créer un bon de réception")
    public ResponseEntity<BonDeReceptionResponse> create(@Valid @RequestBody BonDeReceptionCreateRequest request) {
        return new ResponseEntity<>(bonDeReceptionService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un bon de réception")
    public ResponseEntity<BonDeReceptionResponse> update(@PathVariable Long id,
            @Valid @RequestBody BonDeReceptionUpdateRequest request) {
        return ResponseEntity.ok(bonDeReceptionService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un bon de réception par son ID")
    public ResponseEntity<BonDeReceptionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bonDeReceptionService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les bons de réception")
    public ResponseEntity<List<BonDeReceptionResponse>> getAll() {
        return ResponseEntity.ok(bonDeReceptionService.getAll());
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des bons de réception")
    public ResponseEntity<List<BonDeReceptionResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(bonDeReceptionService.search(keyword));
    }

    @GetMapping("/recent")
    @Operation(summary = "Récupérer les bons de réception récents")
    public ResponseEntity<List<BonDeReceptionResponse>> getRecent() {
        return ResponseEntity.ok(bonDeReceptionService.getRecentBonsDeReception());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un bon de réception")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bonDeReceptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Générer le PDF d'un bon de réception")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = bonDeReceptionService.generatePdf(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "BR_" + id + ".pdf");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
    }
}
