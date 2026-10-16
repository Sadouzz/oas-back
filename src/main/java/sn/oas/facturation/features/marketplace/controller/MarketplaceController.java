package sn.oas.facturation.features.marketplace.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
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
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    @Operation(summary = "Ajouter un produit (Admin)")
    public ResponseEntity<Produit> createProduit(@RequestBody ProduitRequest request) {
        return new ResponseEntity<>(produitService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/produits/{id}")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    @Operation(summary = "Modifier un produit (Admin)")
    public ResponseEntity<Produit> updateProduit(@PathVariable Long id, @RequestBody ProduitRequest request) {
        return ResponseEntity.ok(produitService.update(id, request));
    }

    @DeleteMapping("/produits/{id}")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    @Operation(summary = "Supprimer un produit (Admin)")
    public ResponseEntity<Void> deleteProduit(@PathVariable Long id) {
        produitService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/produits")
    @Operation(summary = "Lister tous les produits")
    public ResponseEntity<List<ProduitListResponse>> getAllProduits() {
        return ResponseEntity.ok(produitService.getDisponibles().stream().map(ProduitListResponse::from).toList());
    }

    @GetMapping("/produits/{id}")
    @Operation(summary = "Voir les détails d'un produit")
    public ResponseEntity<ProduitListResponse> getProduitById(@PathVariable Long id) {
        Produit produit = produitService.getById(id);
        if (Boolean.TRUE.equals(produit.getArchive()) || !Boolean.TRUE.equals(produit.getDisponible())) {
            throw new sn.oas.facturation.shared.exception.ResourceNotFoundException("Produit introuvable avec l'id : " + id);
        }
        return ResponseEntity.ok(ProduitListResponse.from(produit));
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
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
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
            return ResponseEntity.ok(pageResult.map(ProduitListResponse::from));
        }
        return ResponseEntity.ok(produitService.search(keyword).stream().map(ProduitListResponse::from).toList());
    }

    @PatchMapping("/produits/{id}/disponibilite")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    @Operation(summary = "Activer ou désactiver la disponibilité d'un produit (Admin)")
    public ResponseEntity<Produit> toggleDisponibilite(@PathVariable Long id, @RequestParam Boolean disponible) {
        return ResponseEntity.ok(produitService.toggleDisponibilite(id, disponible));
    }

    @PatchMapping("/produits/{id}/archiver")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    @Operation(summary = "Archiver un produit (Admin)")
    public ResponseEntity<Produit> archiverProduit(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.archiver(id));
    }

    @PatchMapping("/produits/{id}/desarchiver")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    @Operation(summary = "Désarchiver un produit (Admin)")
    public ResponseEntity<Produit> desarchiverProduit(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.desarchiver(id));
    }

    // =========================================================================
    // SECTION 2 : DEMANDES DE PRODUITS (Clients & Traitement Admin)
    // =========================================================================

    @PostMapping("/demandes")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ROLE_CLIENT')")
    @Operation(summary = "Client: faire une demande d'achat de produit")
    public ResponseEntity<DemandeProduitListResponse> createDemande(@Valid @RequestBody DemandeProduitRequest request) {
        Client client = clientService.getClientConnecte();
        return new ResponseEntity<>(DemandeProduitListResponse.from(demandeProduitService.create(client, request)), HttpStatus.CREATED);
    }

    @GetMapping("/demandes")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    @Operation(summary = "Admin: voir toutes les demandes / Client: voir ses demandes")
    public ResponseEntity<List<DemandeProduitListResponse>> getAllDemandes() {
        return ResponseEntity.ok(demandeProduitService.getAll().stream().map(DemandeProduitListResponse::from).toList());
    }

    @GetMapping("/demandes/mes-demandes")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ROLE_CLIENT')")
    @Operation(summary = "Client: voir ses propres demandes d'achat")
    public ResponseEntity<List<DemandeProduitListResponse>> getMesDemandes() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(demandeProduitService.getByClient(client).stream().map(DemandeProduitListResponse::from).toList());
    }

    @GetMapping("/demandes/{id}")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    @Operation(summary = "Voir le détail d'une demande par ID")
    public ResponseEntity<DemandeProduitListResponse> getDemandeById(@PathVariable Long id) {
        return ResponseEntity.ok(DemandeProduitListResponse.from(demandeProduitService.getById(id)));
    }

    @GetMapping("/demandes/historique")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    @Operation(summary = "Voir l'historique complet des demandes")
    public ResponseEntity<List<DemandeProduitListResponse>> getHistoriqueDemandes() {
        return ResponseEntity.ok(demandeProduitService.getHistoriqueTous().stream().map(DemandeProduitListResponse::from).toList());
    }

    @GetMapping("/demandes/client/historique")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ROLE_CLIENT')")
    @Operation(summary = "Client: voir l'historique de ses propres demandes")
    public ResponseEntity<List<DemandeProduitListResponse>> getHistoriqueClient() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(demandeProduitService.getHistoriqueByClient(client).stream().map(DemandeProduitListResponse::from).toList());
    }

    @PatchMapping("/demandes/{id}/annuler")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ROLE_CLIENT')")
    @Operation(summary = "Client: annuler une demande en attente")
    public ResponseEntity<DemandeProduitListResponse> annulerDemande(@PathVariable Long id) {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(DemandeProduitListResponse.from(demandeProduitService.cancel(id, client)));
    }

    @PutMapping("/{id}/valider")
    @PatchMapping("/demandes/{id}/accepter")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    @Operation(summary = "Admin: valider / accepter une demande")
    public ResponseEntity<DemandeProduitListResponse> validerDemande(@PathVariable Long id) {
        return ResponseEntity.ok(DemandeProduitListResponse.from(demandeProduitService.updateStatus(id, "ACCEPTEE")));
    }

    @PatchMapping("/demandes/{id}/refuser")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    @Operation(summary = "Admin: refuser une demande")
    public ResponseEntity<DemandeProduitListResponse> refuserDemande(@PathVariable Long id) {
        return ResponseEntity.ok(DemandeProduitListResponse.from(demandeProduitService.updateStatus(id, "REFUSEE")));
    }

    @PatchMapping("/demandes/{id}/en-cours")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    @Operation(summary = "Admin: mettre une demande en attente/en cours")
    public ResponseEntity<DemandeProduitListResponse> enCoursDemande(@PathVariable Long id) {
        return ResponseEntity.ok(DemandeProduitListResponse.from(demandeProduitService.updateStatus(id, "EN_ATTENTE")));
    }

    @PutMapping("/demandes/{id}/cloturer")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    @Operation(summary = "Admin: clôturer / annuler une demande")
    public ResponseEntity<DemandeProduitListResponse> cloturerDemande(@PathVariable Long id) {
        return ResponseEntity.ok(DemandeProduitListResponse.from(demandeProduitService.updateStatus(id, "ANNULEE")));
    }

    @PutMapping("/demandes/{id}/commander")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    @Operation(summary = "Admin: marquer une demande comme commandée")
    public ResponseEntity<DemandeProduitListResponse> commanderDemande(@PathVariable Long id) {
        return ResponseEntity.ok(DemandeProduitListResponse.from(demandeProduitService.updateStatus(id, "COMMANDEE")));
    }

    @PutMapping("/demandes/{id}/livrer")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    @Operation(summary = "Admin: marquer une demande comme livrée")
    public ResponseEntity<DemandeProduitListResponse> livrerDemande(@PathVariable Long id) {
        return ResponseEntity.ok(DemandeProduitListResponse.from(demandeProduitService.updateStatus(id, "LIVREE")));
    }
}
