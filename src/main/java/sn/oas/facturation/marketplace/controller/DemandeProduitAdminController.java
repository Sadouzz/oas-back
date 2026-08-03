package sn.oas.facturation.marketplace.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.marketplace.data.entity.DemandeProduit;
import sn.oas.facturation.marketplace.service.DemandeProduitService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/marketplace/demandes")
@RequiredArgsConstructor
@Tag(name = "Marketplace Demandes Admin", description = "Gestion des demandes marketplace côté admin")
public class DemandeProduitAdminController {

    private final DemandeProduitService demandeProduitService;

    @GetMapping
    @Operation(summary = "Admin: voir toutes les demandes")
    public ResponseEntity<List<DemandeProduit>> getAll() {
        return ResponseEntity.ok(demandeProduitService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Admin: voir les détails d'une demande")
    public ResponseEntity<DemandeProduit> getById(@PathVariable Long id) {
        return ResponseEntity.ok(demandeProduitService.getById(id));
    }

    @GetMapping("/historique")
    @Operation(summary = "Admin: voir l'historique des demandes")
    public ResponseEntity<List<DemandeProduit>> getHistorique() {
        return ResponseEntity.ok(demandeProduitService.getHistoriqueTous());
    }

    @PutMapping("/{id}/valider")
    @Operation(summary = "Admin: valider une demande")
    public ResponseEntity<DemandeProduit> valider(@PathVariable Long id) {
        return ResponseEntity.ok(demandeProduitService.updateStatus(id, "ACCEPTEE"));
    }

    @PatchMapping("/{id}/accepter")
    @Operation(summary = "Admin: accepter une demande")
    public ResponseEntity<DemandeProduit> accepter(@PathVariable Long id) {
        return ResponseEntity.ok(demandeProduitService.updateStatus(id, "ACCEPTEE"));
    }

    @PatchMapping("/{id}/refuser")
    @Operation(summary = "Admin: refuser une demande")
    public ResponseEntity<DemandeProduit> refuser(@PathVariable Long id) {
        return ResponseEntity.ok(demandeProduitService.updateStatus(id, "REFUSEE"));
    }

    @PatchMapping("/{id}/en-cours")
    @Operation(summary = "Admin: mettre une demande en cours")
    public ResponseEntity<DemandeProduit> enCours(@PathVariable Long id) {
        return ResponseEntity.ok(demandeProduitService.updateStatus(id, "EN_ATTENTE"));
    }

    @PutMapping("/{id}/cloturer")
    @Operation(summary = "Admin: clôturer une demande")
    public ResponseEntity<DemandeProduit> cloturer(@PathVariable Long id) {
        return ResponseEntity.ok(demandeProduitService.updateStatus(id, "ANNULEE"));
    }

    @PutMapping("/{id}/commander")
    @Operation(summary = "Admin: marquer une demande comme commandée")
    public ResponseEntity<DemandeProduit> commander(@PathVariable Long id) {
        return ResponseEntity.ok(demandeProduitService.updateStatus(id, "COMMANDEE"));
    }

    @PutMapping("/{id}/livrer")
    @Operation(summary = "Admin: marquer une demande comme livrée")
    public ResponseEntity<DemandeProduit> livrer(@PathVariable Long id) {
        return ResponseEntity.ok(demandeProduitService.updateStatus(id, "LIVREE"));
    }
}
