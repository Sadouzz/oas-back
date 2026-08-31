package sn.oas.facturation.avoirHT.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.avoirHT.dto.AvoirHTCreateRequest;
import sn.oas.facturation.avoirHT.dto.AvoirHTResponse;
import sn.oas.facturation.avoirHT.service.AvoirHTService;

import java.util.List;

@RestController
@RequestMapping("/api/avoirs-ht")
@RequiredArgsConstructor
@Tag(name = "Avoirs HT", description = "API pour la gestion des avoirs Hors Taxe")
public class AvoirHTController {

    private final AvoirHTService avoirHTService;

    @PostMapping
    @Operation(summary = "Créer un avoir HT")
    public ResponseEntity<AvoirHTResponse> create(@RequestBody AvoirHTCreateRequest request) {
        return new ResponseEntity<>(avoirHTService.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un avoir HT par son ID")
    public ResponseEntity<AvoirHTResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(avoirHTService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les avoirs HT")
    public ResponseEntity<List<AvoirHTResponse>> getAll() {
        return ResponseEntity.ok(avoirHTService.getAll());
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des avoirs HT")
    public ResponseEntity<List<AvoirHTResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(avoirHTService.search(keyword));
    }

    @GetMapping("/recent")
    @Operation(summary = "Récupérer les avoirs HT récents")
    public ResponseEntity<List<AvoirHTResponse>> getRecent() {
        return ResponseEntity.ok(avoirHTService.getRecentAvoirs());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un avoir HT")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        avoirHTService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Générer le PDF d'un avoir HT")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = avoirHTService.generatePdf(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "AvoirHT_" + id + ".pdf");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
    }
}
