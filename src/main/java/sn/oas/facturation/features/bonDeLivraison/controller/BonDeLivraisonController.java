package sn.oas.facturation.features.bonDeLivraison.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonCreateRequest;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonFactureRequest;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonOrdreContextResponse;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonResponse;
import sn.oas.facturation.features.bonDeLivraison.dto.FactureDepuisOrdreResponse;
import sn.oas.facturation.features.bonDeLivraison.service.BonDeLivraisonService;
import sn.oas.facturation.features.bonDeSortie.dto.BonDeSortieDisponibleResponse;
import sn.oas.facturation.features.facture.dto.FactureResponse;
import sn.oas.facturation.features.noteDePrix.dto.NoteDePrixResponse;

import java.util.List;

@RestController
@RequestMapping("/api/bons-de-livraison")
@RequiredArgsConstructor
@Tag(name = "Bons de livraison", description = "Livraisons de véhicules depuis les bons de sortie d'un ordre de réparation")
@PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
public class BonDeLivraisonController {

    private final BonDeLivraisonService service;

    @Operation(summary = "Créer un bon de livraison", description = "Crée un bon de livraison à partir des bons de sortie validés d'un ordre de réparation.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bon de livraison créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou bons de sortie non valides")
    })
    @PostMapping
    public ResponseEntity<BonDeLivraisonResponse> create(@Valid @RequestBody BonDeLivraisonCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Lister les bons de livraison", description = "Retourne la liste paginée des bons de livraison avec recherche optionnelle par mot-clé.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste paginée récupérée avec succès")
    })
    @GetMapping
    public Page<BonDeLivraisonResponse> list(
            @Parameter(description = "Numéro de page (0 par défaut)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page (10 par défaut)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Mot-clé de recherche (numéro, client, immatriculation)") @RequestParam(required = false) String keyword) {
        return service.list(page, size, keyword);
    }

    @Operation(summary = "Obtenir un bon de livraison par ID", description = "Retourne le détail complet d'un bon de livraison incluant ses lignes de pièces et de main-d'œuvre.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bon de livraison trouvé"),
            @ApiResponse(responseCode = "404", description = "Bon de livraison introuvable")
    })
    @GetMapping("/{id}")
    public BonDeLivraisonResponse get(@Parameter(description = "Identifiant du bon de livraison") @PathVariable Long id) {
        return service.get(id);
    }

    @Operation(summary = "Télécharger le PDF d'un bon de livraison", description = "Génère et renvoie le document PDF du bon de livraison selon la disposition demandée.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fichier PDF généré avec succès"),
            @ApiResponse(responseCode = "404", description = "Bon de livraison introuvable")
    })
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(
            @Parameter(description = "Identifiant du bon de livraison") @PathVariable Long id,
            @Parameter(description = "Format d'en-tête (AVEC_ENTETE ou SANS_ENTETE)") @RequestParam(defaultValue = "AVEC_ENTETE") String layoutKey) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=BL_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(service.generatePdf(id, layoutKey));
    }

    @Operation(summary = "Lister les bons de sortie disponibles pour un OR", description = "Retourne les bons de sortie validés et rattachables à un bon de livraison pour l'ordre de réparation spécifié.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bons de sortie disponibles récupérés avec succès"),
            @ApiResponse(responseCode = "404", description = "Ordre de réparation introuvable")
    })
    @GetMapping("/ordres-reparation/{ordreId}/bons-de-sortie")
    public List<BonDeSortieDisponibleResponse> bonsDeSortie(
            @Parameter(description = "Identifiant de l'ordre de réparation") @PathVariable Long ordreId) {
        return service.bonsDeSortieDisponibles(ordreId);
    }

    @Operation(summary = "Obtenir le contexte de livraison d'un OR", description = "Retourne les informations contextuelles de l'ordre de réparation pour la préparation du BL.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contexte de livraison récupéré avec succès"),
            @ApiResponse(responseCode = "404", description = "Ordre de réparation introuvable")
    })
    @GetMapping("/ordres-reparation/{ordreId}/contexte")
    public BonDeLivraisonOrdreContextResponse contexteOrdre(
            @Parameter(description = "Identifiant de l'ordre de réparation") @PathVariable Long ordreId) {
        return service.contexteOrdre(ordreId);
    }

    @Operation(summary = "Créer une facture depuis un ordre de réparation", description = "Génère la facture finale ou note de prix directement à partir de l'OR et passe son statut à FACTURATION.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Facture créée avec succès depuis l'OR"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou statut d'ordre incompatible"),
            @ApiResponse(responseCode = "404", description = "Ordre de réparation introuvable")
    })
    @PostMapping("/ordres-reparation/{ordreId}/facture")
    public FactureDepuisOrdreResponse creerFactureDepuisOrdre(
            @Parameter(description = "Identifiant de l'ordre de réparation") @PathVariable Long ordreId,
            @RequestBody(required = false) BonDeLivraisonFactureRequest request) {
        var facture = service.creerFactureDepuisOrdre(ordreId, request);
        return new FactureDepuisOrdreResponse(facture.getId(), facture.getNumero());
    }

    @Operation(summary = "Convertir un bon de livraison en facture", description = "Convertit le bon de livraison en facture commerciale (TTC ou HT) et fait passer l'ordre de réparation à l'étape FACTURATION.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bon de livraison converti en facture avec succès"),
            @ApiResponse(responseCode = "400", description = "Impossible de convertir le bon de livraison"),
            @ApiResponse(responseCode = "404", description = "Bon de livraison introuvable")
    })
    @PostMapping("/{id}/convertir-en-facture")
    public FactureResponse convertirEnFacture(
            @Parameter(description = "Identifiant du bon de livraison") @PathVariable Long id,
            @RequestBody(required = false) BonDeLivraisonFactureRequest request) {
        return FactureResponse.from(service.convertirEnFacture(id, request));
    }

    @Operation(summary = "Convertir un bon de livraison en note de prix", description = "Convertit le bon de livraison en note de prix interne et fait passer l'ordre de réparation à l'étape FACTURATION.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bon de livraison converti en note de prix avec succès"),
            @ApiResponse(responseCode = "400", description = "Impossible de convertir le bon de livraison"),
            @ApiResponse(responseCode = "404", description = "Bon de livraison introuvable")
    })
    @PostMapping("/{id}/convertir-en-note-de-prix")
    public NoteDePrixResponse convertirEnNote(
            @Parameter(description = "Identifiant du bon de livraison") @PathVariable Long id,
            @RequestBody(required = false) BonDeLivraisonFactureRequest request) {
        return NoteDePrixResponse.from(service.convertirEnNoteDePrix(id, request));
    }
}
