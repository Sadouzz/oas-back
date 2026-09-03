package sn.oas.facturation.features.devisPrevisionnel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.client.service.ClientService;
import sn.oas.facturation.features.devisPrevisionnel.data.entity.DevisPrevisionnel;
import sn.oas.facturation.features.devisPrevisionnel.dto.DevisPrevisionnelListResponse;
import sn.oas.facturation.features.devisPrevisionnel.dto.DevisPrevisionnelRequest;
import sn.oas.facturation.features.devisPrevisionnel.service.DevisPrevisionnelService;

import java.util.List;

@Tag(name = "Devis prévisionnels", description = "Gestion des devis prévisionnels")
@RestController
@RequestMapping("/api/devis-previsionnels")
@RequiredArgsConstructor
public class DevisPrevisionnelController {

    private final DevisPrevisionnelService devisPrevisionnelService;
    private final ClientService clientService;

    @Operation(summary = "Créer un devis prévisionnel")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devis créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Client ou véhicule invalide")
    })
    @PostMapping
    public ResponseEntity<DevisPrevisionnel> creer(@RequestBody DevisPrevisionnelRequest request) {
        return new ResponseEntity<>(devisPrevisionnelService.creer(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Modifier un devis prévisionnel")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devis modifié avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou devis introuvable")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DevisPrevisionnel> modifier(@PathVariable Long id,
            @RequestBody DevisPrevisionnelRequest request) {
        return ResponseEntity.ok(devisPrevisionnelService.modifier(id, request));
    }

    @Operation(summary = "Supprimer un devis prévisionnel")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Devis supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Devis introuvable")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        devisPrevisionnelService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtenir un devis par son ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devis trouvé"),
            @ApiResponse(responseCode = "404", description = "Devis introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DevisPrevisionnel> getById(@PathVariable Long id) {
        return ResponseEntity.ok(devisPrevisionnelService.getById(id));
    }

    @Operation(summary = "Récupérer les détails d'un devis prévisionnel")
    @GetMapping("/{id}/details")
    public ResponseEntity<DevisPrevisionnel> getDetails(@PathVariable Long id) {
        return ResponseEntity.ok(devisPrevisionnelService.getById(id));
    }

    @Operation(summary = "Obtenir un devis par l'ID de la fiche atelier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devis trouvé"),
            @ApiResponse(responseCode = "204", description = "Aucun devis lié à cette fiche atelier")
    })
    @GetMapping("/fiche-atelier/{ficheAtelierId}")
    public ResponseEntity<DevisPrevisionnel> getByFicheAtelierId(@PathVariable Long ficheAtelierId) {
        return devisPrevisionnelService.getByFicheAtelierId(ficheAtelierId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Lister les devis prévisionnels", description = "Retourne tous les devis. Filtrable par clientId, vehiculeId ou keyword, avec pagination.")
    @ApiResponse(responseCode = "200", description = "Liste retournée")
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehiculeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (keyword != null && !keyword.trim().isEmpty())
            return ResponseEntity.ok(devisPrevisionnelService.search(keyword.trim(), page, size).map(DevisPrevisionnelListResponse::from));
        if (clientId != null)
            return ResponseEntity.ok(devisPrevisionnelService.getByClient(clientId).stream().map(DevisPrevisionnelListResponse::from).toList());
        if (vehiculeId != null)
            return ResponseEntity.ok(devisPrevisionnelService.getByVehicule(vehiculeId).stream().map(DevisPrevisionnelListResponse::from).toList());
        return ResponseEntity.ok(devisPrevisionnelService.getAll(page, size).map(DevisPrevisionnelListResponse::from));
    }

    @Operation(summary = "Rechercher des devis prévisionnels")
    @GetMapping("/search")
    public ResponseEntity<List<DevisPrevisionnelListResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(devisPrevisionnelService.search(keyword).stream().map(DevisPrevisionnelListResponse::from).toList());
    }

    @Operation(summary = "Générer le PDF d'un devis prévisionnel")
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {
        byte[] pdfBytes = devisPrevisionnelService.generatePdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Devis_" + id + ".pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @Operation(summary = "Valider un devis prévisionnel")
    @PutMapping("/{id}/valider")
    public ResponseEntity<DevisPrevisionnel> valider(@PathVariable Long id) {
        return ResponseEntity.ok(devisPrevisionnelService.valider(id));
    }

    @Operation(summary = "Annuler un devis prévisionnel")
    @PutMapping("/{id}/annuler")
    public ResponseEntity<DevisPrevisionnel> annuler(@PathVariable Long id) {
        return ResponseEntity.ok(devisPrevisionnelService.annuler(id));
    }

    // --- Client endpoints ---
    @Operation(summary = "Lister les devis prévisionnels du client connecté")
    @GetMapping("/me")
    public ResponseEntity<List<DevisPrevisionnelListResponse>> getMyDevis() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(devisPrevisionnelService.getClientDevis(client).stream().map(DevisPrevisionnelListResponse::from).toList());
    }

    @Operation(summary = "Accepter un devis prévisionnel par le client")
    @PutMapping("/{id}/client-accepter")
    public ResponseEntity<DevisPrevisionnel> clientAccepter(@PathVariable Long id) {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(devisPrevisionnelService.clientAccepter(client, id));
    }

    @Operation(summary = "Refuser un devis prévisionnel par le client")
    @PutMapping("/{id}/client-refuser")
    public ResponseEntity<DevisPrevisionnel> clientRefuser(@PathVariable Long id) {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(devisPrevisionnelService.clientRefuser(client, id));
    }
}
