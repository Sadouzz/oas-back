package sn.oas.facturation.bonDeCommande.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.bonDeCommande.dto.BonDeCommandeCreateRequest;
import sn.oas.facturation.bonDeCommande.dto.BonDeCommandeResponse;
import sn.oas.facturation.bonDeCommande.dto.BonDeCommandeUpdateRequest;
import sn.oas.facturation.bonDeCommande.service.BonDeCommandeService;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/bons-de-commande")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class BonDeCommandeController {

    private final BonDeCommandeService bonDeCommandeService;

    @PostMapping
    public ResponseEntity<BonDeCommandeResponse> create(@Valid @RequestBody BonDeCommandeCreateRequest request) {
        return new ResponseEntity<>(bonDeCommandeService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BonDeCommandeResponse> update(@PathVariable Long id, @Valid @RequestBody BonDeCommandeUpdateRequest request) {
        return ResponseEntity.ok(bonDeCommandeService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BonDeCommandeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bonDeCommandeService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<BonDeCommandeResponse>> getAll() {
        return ResponseEntity.ok(bonDeCommandeService.getAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<BonDeCommandeResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(bonDeCommandeService.search(keyword));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<BonDeCommandeResponse>> getRecentBonDeCommandes() {
        return ResponseEntity.ok(bonDeCommandeService.getRecentBonDeCommandes());
    }

    @PostMapping("/{id}/envoyer")
    public ResponseEntity<BonDeCommandeResponse> envoyer(@PathVariable Long id) {
        return ResponseEntity.ok(bonDeCommandeService.envoyer(id));
    }

    @PostMapping("/{id}/receptionner")
    public ResponseEntity<BonDeCommandeResponse> receptionner(@PathVariable Long id) {
        return ResponseEntity.ok(bonDeCommandeService.receptionner(id));
    }

    @PostMapping("/{id}/annuler")
    public ResponseEntity<BonDeCommandeResponse> annuler(@PathVariable Long id) {
        return ResponseEntity.ok(bonDeCommandeService.annuler(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bonDeCommandeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> genererPdf(@PathVariable Long id) {
        byte[] pdfBytes = bonDeCommandeService.generatePdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "bon_de_commande_" + id + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
