package sn.oas.facturation.features.ficheAtelier.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sn.oas.facturation.features.ficheAtelier.data.dto.FicheAtelierDetailResponse;
import sn.oas.facturation.features.ficheAtelier.data.dto.FicheAtelierListResponse;
import sn.oas.facturation.features.ficheAtelier.data.dto.FicheAtelierRequest;
import sn.oas.facturation.features.ficheAtelier.service.FicheAtelierService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/fiches-atelier")
@RequiredArgsConstructor
public class FicheAtelierController {

    private final FicheAtelierService ficheAtelierService;

    @PostMapping
    public ResponseEntity<FicheAtelierDetailResponse> create(@Valid @RequestBody FicheAtelierRequest request) {
        return new ResponseEntity<>(FicheAtelierDetailResponse.from(ficheAtelierService.create(request)),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FicheAtelierDetailResponse> update(@PathVariable Long id,
            @Valid @RequestBody FicheAtelierRequest request) {
        return ResponseEntity.ok(FicheAtelierDetailResponse.from(ficheAtelierService.update(id, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FicheAtelierDetailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(FicheAtelierDetailResponse.from(ficheAtelierService.getById(id)));
    }

    @GetMapping("/rendezvous/{rendezVousId}")
    public ResponseEntity<FicheAtelierDetailResponse> getByRendezVousId(@PathVariable Long rendezVousId) {
        var fiche = ficheAtelierService.getByRendezVousId(rendezVousId);
        if (fiche == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(FicheAtelierDetailResponse.from(fiche));
    }

    @GetMapping
    public ResponseEntity<Page<FicheAtelierListResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ficheAtelierService.getAll(page, size).map(FicheAtelierListResponse::from));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ficheAtelierService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/signature-sortie")
    public ResponseEntity<FicheAtelierDetailResponse> signForExit(@PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String signature = request.get("signature");
        if (signature == null || signature.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(FicheAtelierDetailResponse.from(ficheAtelierService.signForExit(id, signature)));
    }
}
