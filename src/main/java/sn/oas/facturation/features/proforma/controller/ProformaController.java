package sn.oas.facturation.features.proforma.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.service.ClientService;
import sn.oas.facturation.features.facture.dto.FactureResponse;
import sn.oas.facturation.features.proforma.dto.ProformaCreateRequest;
import sn.oas.facturation.features.proforma.dto.ProformaListResponse;
import sn.oas.facturation.features.proforma.dto.ProformaResponse;
import sn.oas.facturation.features.proforma.dto.ProformaUpdateRequest;
import sn.oas.facturation.features.proforma.service.ProformaService;

import java.util.List;

@RestController
@RequestMapping("/api/proformas")
@RequiredArgsConstructor
@Tag(name = "Proforma", description = "API pour la gestion des factures proforma")
public class ProformaController {

    private final ProformaService proformaService;
    private final ClientService clientService;

    @PostMapping
    @Operation(summary = "Créer un proforma")
    public ResponseEntity<ProformaResponse> create(@Valid @RequestBody ProformaCreateRequest request) {
        return new ResponseEntity<>(ProformaResponse.from(proformaService.create(request)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un proforma")
    public ResponseEntity<ProformaResponse> update(@PathVariable Long id, @Valid @RequestBody ProformaUpdateRequest request) {
        return ResponseEntity.ok(ProformaResponse.from(proformaService.update(id, request)));
    }

    @PutMapping("/{id}/valider")
    @Operation(summary = "Valider un proforma (accord client)")
    public ResponseEntity<ProformaResponse> valider(@PathVariable Long id) {
        return ResponseEntity.ok(ProformaResponse.from(proformaService.valider(id)));
    }

    @PostMapping("/{id}/valider-envoi")
    @Operation(summary = "Valider les prix par le chef d'atelier et rendre le proforma visible/envoyé au client")
    public ResponseEntity<ProformaResponse> validerEnvoi(@PathVariable Long id) {
        return ResponseEntity.ok(ProformaResponse.from(proformaService.validerEnvoi(id)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un proforma par son ID")
    public ResponseEntity<ProformaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ProformaResponse.from(proformaService.getById(id)));
    }

    @GetMapping("/ordre-reparation/{ordreReparationId}")
    @Operation(summary = "Récupérer le proforma lié à une fiche atelier")
    public ResponseEntity<ProformaResponse> getByOrdreReparationId(@PathVariable Long ordreReparationId) {
        var p = proformaService.getByOrdreReparationId(ordreReparationId);
        return p != null ? ResponseEntity.ok(ProformaResponse.from(p)) : ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les proformas ou rechercher par mot-clé avec pagination")
    public ResponseEntity<Page<ProformaListResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(proformaService.search(keyword.trim(), page, size).map(ProformaListResponse::from));
        }
        return ResponseEntity.ok(proformaService.getAll(page, size).map(ProformaListResponse::from));
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des proformas")
    public ResponseEntity<List<ProformaListResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(proformaService.search(keyword).stream().map(ProformaListResponse::from).toList());
    }

    @GetMapping("/recent")
    @Operation(summary = "Récupérer les proformas récents")
    public ResponseEntity<List<ProformaListResponse>> getRecent() {
        return ResponseEntity.ok(proformaService.getRecentProformas().stream().map(ProformaListResponse::from).toList());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un proforma")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        proformaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Générer le PDF d'un proforma")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {
        byte[] pdfBytes = proformaService.generatePdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Proforma_" + id + ".pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/{id}/convert")
    @Operation(summary = "Convertir un proforma en facture finale")
    public ResponseEntity<FactureResponse> convertToFacture(@PathVariable Long id) {
        return ResponseEntity.ok(FactureResponse.from(proformaService.convertToFacture(id)));
    }

    // --- Client endpoints ---
    @GetMapping("/me")
    @Operation(summary = "Lister les proformas du client connecté")
    public ResponseEntity<List<ProformaListResponse>> getMyProformas() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(proformaService.getClientProformas(client).stream().map(ProformaListResponse::from).toList());
    }

    @PutMapping("/{id}/client-valider")
    @Operation(summary = "Valider un proforma par le client")
    public ResponseEntity<ProformaResponse> clientValider(@PathVariable Long id) {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(ProformaResponse.from(proformaService.clientValider(client, id)));
    }

    @PutMapping("/{id}/client-refuser")
    @Operation(summary = "Refuser un proforma par le client")
    public ResponseEntity<ProformaResponse> clientRefuser(@PathVariable Long id) {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(ProformaResponse.from(proformaService.clientRefuser(client, id)));
    }
}
