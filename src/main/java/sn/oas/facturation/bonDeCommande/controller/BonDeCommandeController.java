package sn.oas.facturation.bonDeCommande.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.bonDeCommande.dto.BonDeCommandeCreateRequest;
import sn.oas.facturation.bonDeCommande.dto.ReceptionBonDeCommandeRequest;
import sn.oas.facturation.bonDeCommande.dto.BonDeCommandeResponse;
import sn.oas.facturation.bonDeCommande.dto.BonDeCommandeUpdateRequest;
import sn.oas.facturation.bonDeCommande.service.BonDeCommandeService;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/bons-de-commande")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Bons de commande", description = "API pour la gestion des bons de commande")
public class BonDeCommandeController {

    private final BonDeCommandeService bonDeCommandeService;

    @PostMapping
    @Operation(summary = "Créer un nouveau bon de commande")
    public ResponseEntity<BonDeCommandeResponse> create(@Valid @RequestBody BonDeCommandeCreateRequest request) {
        return new ResponseEntity<>(bonDeCommandeService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un bon de commande")
    public ResponseEntity<BonDeCommandeResponse> update(@PathVariable Long id, @Valid @RequestBody BonDeCommandeUpdateRequest request) {
        return ResponseEntity.ok(bonDeCommandeService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un bon de commande par son ID")
    public ResponseEntity<BonDeCommandeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bonDeCommandeService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Lister tous les bons de commande")
    public ResponseEntity<List<BonDeCommandeResponse>> getAll() {
        return ResponseEntity.ok(bonDeCommandeService.getAll());
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des bons de commande")
    public ResponseEntity<List<BonDeCommandeResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(bonDeCommandeService.search(keyword));
    }

    @GetMapping("/recent")
    @Operation(summary = "Récupérer les bons de commande récents")
    public ResponseEntity<List<BonDeCommandeResponse>> getRecentBonDeCommandes() {
        return ResponseEntity.ok(bonDeCommandeService.getRecentBonDeCommandes());
    }

    @PostMapping("/{id}/envoyer")
    @Operation(summary = "Envoyer un bon de commande")
    public ResponseEntity<BonDeCommandeResponse> envoyer(@PathVariable Long id) {
        return ResponseEntity.ok(bonDeCommandeService.envoyer(id));
    }

    @PostMapping("/{id}/receptionner")
    @Operation(summary = "Réceptionner un bon de commande")
    public ResponseEntity<BonDeCommandeResponse> receptionner(@PathVariable Long id) {
        return ResponseEntity.ok(bonDeCommandeService.receptionner(id));
    }

    @PostMapping({"/{id}/receptionner-reception", "/{id}/receptionner-livraison"})
    @Operation(summary = "Réceptionner un bon de commande avec les quantités du bon de réception")
    public ResponseEntity<BonDeCommandeResponse> receptionnerAvecReception(
            @PathVariable Long id,
            @RequestBody ReceptionBonDeCommandeRequest request) {
        return ResponseEntity.ok(bonDeCommandeService.receptionnerAvecQuantites(id, request));
    }

    @PostMapping("/{id}/assigner-fournisseur")
    @Operation(summary = "Assigner un fournisseur à un bon de commande en attente")
    public ResponseEntity<BonDeCommandeResponse> assignerFournisseur(
            @PathVariable Long id,
            @RequestParam Long fournisseurId) {
        return ResponseEntity.ok(bonDeCommandeService.assignerFournisseur(id, fournisseurId));
    }

    @PostMapping("/{id}/annuler")
    @Operation(summary = "Annuler un bon de commande")
    public ResponseEntity<BonDeCommandeResponse> annuler(@PathVariable Long id) {
        return ResponseEntity.ok(bonDeCommandeService.annuler(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un bon de commande")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bonDeCommandeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Générer le PDF d'un bon de commande")
    public ResponseEntity<byte[]> genererPdf(@PathVariable Long id) {
        byte[] pdfBytes = bonDeCommandeService.generatePdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "bon_de_commande_" + id + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
