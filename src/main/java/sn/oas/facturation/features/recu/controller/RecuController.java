package sn.oas.facturation.features.recu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.recu.dto.RecuDetailResponse;
import sn.oas.facturation.features.recu.dto.RecuListItemResponse;
import sn.oas.facturation.features.recu.dto.VersementClientRequest;
import sn.oas.facturation.features.recu.service.RecuService;

@RestController
@RequestMapping("/api/recus")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
@Tag(name = "Reçus", description = "Reçus clients : reçus en cours, reçus payés, versements")
public class RecuController {

    private final RecuService recuService;

    @GetMapping("/en-cours")
    @Operation(summary = "Lister les reçus en cours (recherche par numéro de reçu ou par numéro client)")
    public ResponseEntity<Page<RecuListItemResponse>> getRecusEnCours(
            @RequestParam(required = false) String numeroRecu,
            @RequestParam(required = false) String numeroClient,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(recuService.getRecusEnCours(numeroRecu, numeroClient, page, size));
    }

    @GetMapping("/payes")
    @Operation(summary = "Lister les reçus payés (recherche par numéro de reçu ou par numéro client)")
    public ResponseEntity<Page<RecuListItemResponse>> getRecusPayes(
            @RequestParam(required = false) String numeroRecu,
            @RequestParam(required = false) String numeroClient,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(recuService.getRecusPayes(numeroRecu, numeroClient, page, size));
    }

    @PostMapping("/client/{clientId}")
    @Operation(summary = "Ouvrir le reçu en cours d'un client, ou le mettre à jour s'il existe déjà")
    public ResponseEntity<RecuDetailResponse> ouvrirRecu(@PathVariable Long clientId) {
        return ResponseEntity.ok(recuService.ouvrirRecu(clientId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Afficher un reçu : documents, totaux et versements")
    public ResponseEntity<RecuDetailResponse> getRecu(@PathVariable Long id) {
        return ResponseEntity.ok(recuService.getRecu(id));
    }

    @PostMapping("/{id}/versements")
    @Operation(summary = "Enregistrer un versement, réparti sur les documents impayés les plus anciens")
    public ResponseEntity<RecuDetailResponse> enregistrerVersement(
            @PathVariable Long id,
            @Valid @RequestBody VersementClientRequest request) {
        return new ResponseEntity<>(recuService.enregistrerVersement(id, request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/versements/{versementId}")
    @Operation(summary = "Annuler un versement : les documents, le solde prépayé et le reçu sont remis à jour")
    public ResponseEntity<RecuDetailResponse> annulerVersement(
            @PathVariable Long id,
            @PathVariable Long versementId) {
        return ResponseEntity.ok(recuService.annulerVersement(id, versementId));
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Imprimer le reçu (layoutKey : AVEC_ENTETE ou SANS_ENTETE)")
    public ResponseEntity<byte[]> genererPdf(
            @PathVariable Long id,
            @RequestParam(defaultValue = "AVEC_ENTETE") String layoutKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename("Recu_" + id + ".pdf").build());
        return new ResponseEntity<>(recuService.genererPdf(id, layoutKey), headers, HttpStatus.OK);
    }

    @PostMapping("/{id}/valider")
    @Operation(summary = "Valider un reçu payé")
    public ResponseEntity<RecuDetailResponse> valider(@PathVariable Long id) {
        return ResponseEntity.ok(recuService.valider(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un reçu en cours sans versement")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        recuService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
