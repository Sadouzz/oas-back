package sn.oas.facturation.features.recu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.service.ClientService;
import sn.oas.facturation.features.recu.dto.RecuRequest;
import sn.oas.facturation.features.recu.dto.RecuResponse;
import sn.oas.facturation.features.recu.service.RecuService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/recus")
@RequiredArgsConstructor
@Tag(name = "Reçus", description = "API pour la gestion des reçus de paiement de facture")
public class RecuController {

    private final RecuService recuService;
    private final ClientService clientService;

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
    @Operation(summary = "Lister tous les reçus de paiement")
    public ResponseEntity<List<RecuResponse>> getAllRecus() {
        return ResponseEntity.ok(recuService.getAll().stream().map(RecuResponse::from).toList());
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CLIENT')")
    @Operation(summary = "Lister l'historique des reçus du client connecté")
    public ResponseEntity<List<RecuResponse>> getClientRecus() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(recuService.getClientRecus(client).stream().map(RecuResponse::from).toList());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
    @Operation(summary = "Créer un nouveau reçu pour une facture")
    public ResponseEntity<RecuResponse> create(@RequestBody RecuRequest request) {
        return new ResponseEntity<>(RecuResponse.from(recuService.create(request)), HttpStatus.CREATED);
    }

    @PostMapping("/payer")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
    @Operation(summary = "Enregistrer un paiement (total ou partiel) pour une facture")
    public ResponseEntity<RecuResponse> registerPayment(
            @RequestParam Long factureId,
            @RequestParam(required = false) BigDecimal montant,
            @RequestParam String methodePaiement) {
        RecuRequest request = RecuRequest.builder()
                .factureId(factureId)
                .montant(montant)
                .modePaiement(methodePaiement)
                .build();
        return new ResponseEntity<>(RecuResponse.from(recuService.create(request)), HttpStatus.CREATED);
    }

    @GetMapping("/facture/{factureId}")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
    @Operation(summary = "Récupérer les reçus d'une facture spécifique")
    public ResponseEntity<List<RecuResponse>> getByFacture(@PathVariable Long factureId) {
        return ResponseEntity.ok(recuService.getByFacture(factureId).stream().map(RecuResponse::from).toList());
    }
}
