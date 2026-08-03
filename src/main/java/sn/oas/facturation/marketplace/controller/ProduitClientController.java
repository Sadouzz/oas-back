package sn.oas.facturation.marketplace.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.client.service.ClientService;
import sn.oas.facturation.marketplace.data.entity.DemandeProduit;
import sn.oas.facturation.marketplace.data.entity.Produit;
import sn.oas.facturation.marketplace.dto.DemandeProduitRequest;
import sn.oas.facturation.marketplace.service.DemandeProduitService;
import sn.oas.facturation.marketplace.service.ProduitService;

import java.util.List;

@RestController
@RequestMapping("/api/client/marketplace")
@RequiredArgsConstructor
@Tag(name = "Marketplace Client", description = "Endpoints client pour le marketplace")
public class ProduitClientController {

    private final ProduitService produitService;
    private final DemandeProduitService demandeProduitService;
    private final ClientService clientService;

    @GetMapping("/produits")
    @Operation(summary = "Client: voir les produits disponibles")
    public ResponseEntity<List<Produit>> getDisponibles() {
        return ResponseEntity.ok(produitService.getDisponibles());
    }

    @GetMapping("/produits/search")
    @Operation(summary = "Client: rechercher un produit")
    public ResponseEntity<List<Produit>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(produitService.search(keyword));
    }

    @GetMapping("/produits/{id}")
    @Operation(summary = "Client: voir les détails d'un produit")
    public ResponseEntity<Produit> getById(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.getById(id));
    }

    @GetMapping("/produits/populaires")
    @Operation(summary = "Client: voir les produits mis en avant")
    public ResponseEntity<List<Produit>> getPopulaires() {
        return ResponseEntity.ok(produitService.getPopulaires());
    }

    @PostMapping("/demandes")
    @Operation(summary = "Client: faire une demande d'achat")
    public ResponseEntity<DemandeProduit> createDemande(@RequestBody DemandeProduitRequest request) {
        Client client = clientService.getClientConnecte();
        return new ResponseEntity<>(demandeProduitService.create(client, request), HttpStatus.CREATED);
    }

    @GetMapping("/demandes")
    @Operation(summary = "Client: voir ses demandes d'achat")
    public ResponseEntity<List<DemandeProduit>> getMesDemandes() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(demandeProduitService.getByClient(client));
    }

    @GetMapping("/historique")
    @Operation(summary = "Client: voir l'historique de ses demandes")
    public ResponseEntity<List<DemandeProduit>> getHistoriqueClient() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(demandeProduitService.getHistoriqueByClient(client));
    }

    @GetMapping("/demandes/{id}")
    @Operation(summary = "Client: voir le détail d'une demande")
    public ResponseEntity<DemandeProduit> getDemandeById(@PathVariable Long id) {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(demandeProduitService.getByIdAndClient(id, client));
    }

    @PatchMapping("/demandes/{id}/annuler")
    @Operation(summary = "Client: annuler une demande en attente")
    public ResponseEntity<DemandeProduit> annulerDemande(@PathVariable Long id) {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(demandeProduitService.cancel(id, client));
    }
}
