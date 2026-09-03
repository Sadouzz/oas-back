package sn.oas.facturation.features.facture.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.client.service.ClientService;
import sn.oas.facturation.features.facture.dto.FactureCreateRequest;
import sn.oas.facturation.features.facture.dto.FactureListResponse;
import sn.oas.facturation.features.facture.dto.FactureResponse;
import sn.oas.facturation.features.facture.service.FactureService;

import java.util.List;

@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
@Tag(name = "Facture", description = "API pour la gestion des factures finales")
public class FactureController {

    private final FactureService factureService;
    private final ClientService clientService;

    @PostMapping("/creer")
    @Operation(summary = "Créer une nouvelle facture à partir d'une fiche atelier")
    public ResponseEntity<FactureResponse> createFacture(@RequestBody FactureCreateRequest request) {
        return new ResponseEntity<>(FactureResponse.from(factureService.createFacture(request)), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une facture par son ID")
    public ResponseEntity<FactureResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(FactureResponse.from(factureService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "Récupérer toutes les factures ou rechercher par mot-clé avec pagination")
    public ResponseEntity<Page<FactureListResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(factureService.search(keyword.trim(), page, size).map(FactureListResponse::from));
        }
        return ResponseEntity.ok(factureService.getAll(page, size).map(FactureListResponse::from));
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des factures")
    public ResponseEntity<List<FactureListResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(factureService.search(keyword).stream().map(FactureListResponse::from).toList());
    }

    @GetMapping("/recent")
    @Operation(summary = "Récupérer les factures récentes")
    public ResponseEntity<List<FactureListResponse>> getRecent() {
        return ResponseEntity.ok(factureService.getRecentFactures().stream().map(FactureListResponse::from).toList());
    }

    @GetMapping("/me")
    @Operation(summary = "Lister l'historique de facturation du client connecté")
    public ResponseEntity<List<FactureListResponse>> getMyFactures() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(factureService.getClientFactures(client).stream().map(FactureListResponse::from).toList());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une facture")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        factureService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Générer le PDF d'une facture")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {
        byte[] pdfBytes = factureService.generatePdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Facture_" + id + ".pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
