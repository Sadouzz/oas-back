package sn.oas.facturation.features.marketplace.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.service.ClientService;
import sn.oas.facturation.features.marketplace.data.entity.DemandeProduit;
import sn.oas.facturation.features.marketplace.data.entity.Produit;
import sn.oas.facturation.features.marketplace.dto.DemandeProduitListResponse;
import sn.oas.facturation.features.marketplace.dto.DemandeProduitRequest;
import sn.oas.facturation.features.marketplace.dto.ProduitListResponse;
import sn.oas.facturation.features.marketplace.dto.ProduitRequest;
import sn.oas.facturation.features.marketplace.service.DemandeProduitService;
import sn.oas.facturation.features.marketplace.service.ProduitService;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
@Tag(name = "Marketplace", description = "Endpoints unifiés pour les produits et demandes du marketplace (Admin & Client)")
public class MarketplaceController {

    private final ProduitService produitService;
    private final DemandeProduitService demandeProduitService;
    private final ClientService clientService;

    // =========================================================================
    // SECTION 1 : PRODUITS (Catalogue, Recherche, Gestion)
    // =========================================================================

    @PostMapping("/produits")
    @Operation(summary = "Ajouter un produit (Admin)")
    public ResponseEntity<Produit> createProduit(@RequestBody ProduitRequest request) {
        return new ResponseEntity<>(produitService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/produits/{id}")
    @Operation(summary = "Modifier un produit (Admin)")
    public ResponseEntity<Produit> updateProduit(@PathVariable Long id, @RequestBody ProduitRequest request) {
        return ResponseEntity.ok(produitService.update(id, request));
    }

    @DeleteMapping("/produits/{id}")
    @Operation(summary = "Supprimer un produit (Admin)")
    public ResponseEntity<Void> deleteProduit(@PathVariable Long id) {
        produitService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/produits")
    @Operation(summary = "Lister tous les produits")
    public ResponseEntity<List<ProduitListResponse>> getAllProduits() {
        return ResponseEntity.ok(produitService.getAll().stream().map(ProduitListResponse::from).toList());
    }

    @GetMapping("/produits/{id}")
    @Operation(summary = "Voir les détails d'un produit")
    public ResponseEntity<Produit> getProduitById(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.getById(id));
    }

    @GetMapping("/produits/disponibles")
    @Operation(summary = "Voir les produits disponibles à la vente")
    public ResponseEntity<List<ProduitListResponse>> getProduitsDisponibles() {
        return ResponseEntity.ok(produitService.getDisponibles().stream().map(ProduitListResponse::from).toList());
    }

    @GetMapping("/produits/populaires")
    @Operation(summary = "Voir les produits mis en avant / populaires")
    public ResponseEntity<List<ProduitListResponse>> getProduitsPopulaires() {
        return ResponseEntity.ok(produitService.getPopulaires().stream().map(ProduitListResponse::from).toList());
    }

    @GetMapping("/produits/archives")
    @Operation(summary = "Voir les produits archivés (Admin)")
    public ResponseEntity<List<Produit>> getProduitsArchives() {
        return ResponseEntity.ok(produitService.getArchives());
    }

    @GetMapping("/produits/search")
    @Operation(summary = "Rechercher des produits avec pagination optionnelle")
    public ResponseEntity<?> searchProduits(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            Page<Produit> pageResult = produitService.search(keyword, page, size);
            return ResponseEntity.ok(pageResult);
        }
        return ResponseEntity.ok(produitService.search(keyword));
    }

    @PatchMapping("/produits/{id}/disponibilite")
    @Operation(summary = "Activer ou désactiver la disponibilité d'un produit (Admin)")
    public ResponseEntity<Produit> toggleDisponibilite(@PathVariable Long id, @RequestParam Boolean disponible) {
        return ResponseEntity.ok(produitService.toggleDisponibilite(id, disponible));
    }

    @PatchMapping("/produits/{id}/archiver")
    @Operation(summary = "Archiver un produit (Admin)")
    public ResponseEntity<Produit> archiverProduit(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.archiver(id));
    }

    @PatchMapping("/produits/{id}/desarchiver")
    @Operation(summary = "Désarchiver un produit (Admin)")
    public ResponseEntity<Produit> desarchiverProduit(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.desarchiver(id));
    }

    // =========================================================================
    // SECTION 2 : DEMANDES DE PRODUITS (Clients & Traitement Admin)
    // =========================================================================

    @PostMapping("/demandes")
    @Operation(summary = "Client: faire une demande d'achat de produit")
    public ResponseEntity<DemandeProduit> createDemande(@RequestBody DemandeProduitRequest request) {
        Client client = clientService.getClientConnecte();
        return new ResponseEntity<>(demandeProduitService.create(client, request), HttpStatus.CREATED);
    }

    @GetMapping("/demandes")
    @Operation(summary = "Admin: voir toutes les demandes / Client: voir ses demandes")
    public ResponseEntity<List<DemandeProduitListResponse>> getAllDemandes() {
        return ResponseEntity.ok(demandeProduitService.getAll().stream().map(DemandeProduitListResponse::from).toList());
    }

    @GetMapping("/demandes/mes-demandes")
    @Operation(summary = "Client: voir ses propres demandes d'achat")
    public ResponseEntity<List<DemandeProduit>> getMesDemandes() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(demandeProduitService.getByClient(client));
    }

    @GetMapping("/demandes/{id}")
    @Operation(summary = "Voir le détail d'une demande par ID")
    public ResponseEntity<DemandeProduit> getDemandeById(@PathVariable Long id) {
        return ResponseEntity.ok(demandeProduitService.getById(id));
    }

    @GetMapping("/demandes/historique")
    @Operation(summary = "Voir l'historique complet des demandes")
    public ResponseEntity<List<DemandeProduitListResponse>> getHistoriqueDemandes() {
        return ResponseEntity.ok(demandeProduitService.getHistoriqueTous().stream().map(DemandeProduitListResponse::from).toList());
    }

    @GetMapping("/demandes/client/historique")
    @Operation(summary = "Client: voir l'historique de ses propres demandes")
    public ResponseEntity<List<DemandeProduit>> getHistoriqueClient() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(demandeProduitService.getHistoriqueByClient(client));
    }

    @PatchMapping("/demandes/{id}/annuler")
    @Operation(summary = "Client: annuler une demande en attente")
    public ResponseEntity<DemandeProduit> annulerDemande(@PathVariable Long id) {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(demandeProduitService.cancel(id, client));
    }

    @PutMapping("/{id}/valider")
    @PatchMapping("/demandes/{id}/accepter")
    @Operation(summary = "Admin: valider / accepter une demande")
    public ResponseEntity<DemandeProduit> validerDemande(@PathVariable Long id) {
        return ResponseEntity.ok(demandeProduitService.updateStatus(id, "ACCEPTEE"));
    }

    @PatchMapping("/demandes/{id}/refuser")
    @Operation(summary = "Admin: refuser une demande")
    public ResponseEntity<DemandeProduit> refuserDemande(@PathVariable Long id) {
        return ResponseEntity.ok(demandeProduitService.updateStatus(id, "REFUSEE"));
    }

    @PatchMapping("/demandes/{id}/en-cours")
    @Operation(summary = "Admin: mettre une demande en attente/en cours")
    public ResponseEntity<DemandeProduit> enCoursDemande(@PathVariable Long id) {
        return ResponseEntity.ok(demandeProduitService.updateStatus(id, "EN_ATTENTE"));
    }

    @PutMapping("/demandes/{id}/cloturer")
    @Operation(summary = "Admin: clôturer / annuler une demande")
    public ResponseEntity<DemandeProduit> cloturerDemande(@PathVariable Long id) {
        return ResponseEntity.ok(demandeProduitService.updateStatus(id, "ANNULEE"));
    }

    @PutMapping("/demandes/{id}/commander")
    @Operation(summary = "Admin: marquer une demande comme commandée")
    public ResponseEntity<DemandeProduit> commanderDemande(@PathVariable Long id) {
        return ResponseEntity.ok(demandeProduitService.updateStatus(id, "COMMANDEE"));
    }

    @PutMapping("/demandes/{id}/livrer")
    @Operation(summary = "Admin: marquer une demande comme livrée")
    public ResponseEntity<DemandeProduit> livrerDemande(@PathVariable Long id) {
        return ResponseEntity.ok(demandeProduitService.updateStatus(id, "LIVREE"));
    }
}