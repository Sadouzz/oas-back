package sn.oas.facturation.devisPrevisionnel.controller;

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
import sn.oas.facturation.devisPrevisionnel.data.entity.DevisPrevisionnel;
import sn.oas.facturation.devisPrevisionnel.dto.DevisPrevisionnelRequest;
import sn.oas.facturation.devisPrevisionnel.service.DevisPrevisionnelService;

import java.util.List;

@Tag(name = "Devis prévisionnels", description = "Gestion des devis prévisionnels")
@RestController
@RequestMapping("/api/devis-previsionnels")
@RequiredArgsConstructor
public class DevisPrevisionnelController {

    private final DevisPrevisionnelService devisPrevisionnelService;

    @Operation(summary = "Créer un devis prévisionnel")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devis créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Client ou véhicule invalide")
    })
    @PostMapping
    public ResponseEntity<DevisPrevisionnel> creer(@RequestBody DevisPrevisionnelRequest request) {
        return ResponseEntity.ok(devisPrevisionnelService.creer(request));
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
            @ApiResponse(responseCode = "400", description = "Devis introuvable")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        devisPrevisionnelService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtenir un devis prévisionnel par ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devis trouvé"),
            @ApiResponse(responseCode = "400", description = "Devis introuvable")
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

    @Operation(summary = "Lister les devis prévisionnels", description = "Retourne tous les devis. Filtrable par clientId ou vehiculeId.")
    @ApiResponse(responseCode = "200", description = "Liste retournée")
    @GetMapping
    public ResponseEntity<List<DevisPrevisionnel>> getAll(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehiculeId) {

        if (clientId != null)
            return ResponseEntity.ok(devisPrevisionnelService.getByClient(clientId));
        if (vehiculeId != null)
            return ResponseEntity.ok(devisPrevisionnelService.getByVehicule(vehiculeId));
        return ResponseEntity.ok(devisPrevisionnelService.getAll());
    }

    @Operation(summary = "Rechercher des devis prévisionnels")
    @GetMapping("/search")
    public ResponseEntity<List<DevisPrevisionnel>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(devisPrevisionnelService.search(keyword));
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
}
