package sn.oas.facturation.proforma.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.facture.dto.FactureResponse;
import sn.oas.facturation.proforma.dto.ProformaCreateRequest;
import sn.oas.facturation.proforma.dto.ProformaResponse;
import sn.oas.facturation.proforma.dto.ProformaUpdateRequest;
import sn.oas.facturation.proforma.service.ProformaService;

import java.util.List;

@RestController
@RequestMapping("/api/proformas")
@RequiredArgsConstructor
@Tag(name = "Proforma", description = "API pour la gestion des factures proforma")
public class ProformaController {

    private final ProformaService proformaService;

    @PostMapping
    @Operation(summary = "Créer un proforma")
    public ResponseEntity<ProformaResponse> create(@Valid @RequestBody ProformaCreateRequest request) {
        return new ResponseEntity<>(proformaService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un proforma")
    public ResponseEntity<ProformaResponse> update(@PathVariable Long id, @Valid @RequestBody ProformaUpdateRequest request) {
        return ResponseEntity.ok(proformaService.update(id, request));
    }

    @PutMapping("/{id}/valider")
    @Operation(summary = "Valider un proforma (accord client)")
    public ResponseEntity<ProformaResponse> valider(@PathVariable Long id) {
        return ResponseEntity.ok(proformaService.valider(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un proforma par son ID")
    public ResponseEntity<ProformaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(proformaService.getById(id));
    }

    @GetMapping("/fiche-atelier/{ficheAtelierId}")
    @Operation(summary = "Récupérer le proforma lié à une fiche atelier")
    public ResponseEntity<ProformaResponse> getByFicheAtelierId(@PathVariable Long ficheAtelierId) {
        return ResponseEntity.ok(proformaService.getByFicheAtelierId(ficheAtelierId));
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les proformas")
    public ResponseEntity<List<ProformaResponse>> getAll() {
        return ResponseEntity.ok(proformaService.getAll());
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des proformas")
    public ResponseEntity<List<ProformaResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(proformaService.search(keyword));
    }

    @GetMapping("/recent")
    @Operation(summary = "Récupérer les proformas récents")
    public ResponseEntity<List<ProformaResponse>> getRecent() {
        return ResponseEntity.ok(proformaService.getRecentProformas());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un proforma")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        proformaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Générer le PDF d'un proforma")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = proformaService.generatePdf(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Proforma_" + id + ".pdf");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
    }

    @PostMapping("/{id}/convert")
    @Operation(summary = "Convertir un proforma en facture finale")
    public ResponseEntity<FactureResponse> convertToFacture(@PathVariable Long id) {
        return ResponseEntity.ok(proformaService.convertToFacture(id));
    }
}
