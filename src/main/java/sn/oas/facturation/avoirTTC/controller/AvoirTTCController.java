package sn.oas.facturation.avoirTTC.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.avoirTTC.dto.AvoirTTCResponse;
import sn.oas.facturation.avoirTTC.service.AvoirTTCService;

import java.util.List;

@RestController
@RequestMapping("/api/avoirs-ttc")
@RequiredArgsConstructor
@Tag(name = "Avoirs TTC", description = "API pour la gestion des avoirs Toutes Taxes Comprises")
public class AvoirTTCController {

    private final AvoirTTCService avoirTTCService;

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un avoir TTC par son ID")
    public ResponseEntity<AvoirTTCResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(avoirTTCService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les avoirs TTC")
    public ResponseEntity<List<AvoirTTCResponse>> getAll() {
        return ResponseEntity.ok(avoirTTCService.getAll());
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des avoirs TTC")
    public ResponseEntity<List<AvoirTTCResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(avoirTTCService.search(keyword));
    }

    @GetMapping("/recent")
    @Operation(summary = "Récupérer les avoirs TTC récents")
    public ResponseEntity<List<AvoirTTCResponse>> getRecent() {
        return ResponseEntity.ok(avoirTTCService.getRecentAvoirs());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un avoir TTC")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        avoirTTCService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Générer le PDF d'un avoir TTC")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = avoirTTCService.generatePdf(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "AvoirTTC_" + id + ".pdf");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
    }
}
