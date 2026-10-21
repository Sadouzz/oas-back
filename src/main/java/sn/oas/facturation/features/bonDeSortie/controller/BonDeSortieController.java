package sn.oas.facturation.features.bonDeSortie.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.bonDeSortie.data.enums.StatutBon;
import sn.oas.facturation.features.bonDeSortie.dto.BonDeSortieHistoriqueListResponse;
import sn.oas.facturation.features.bonDeSortie.dto.BonDeSortieListResponse;
import sn.oas.facturation.features.bonDeSortie.dto.BonDeSortieRequest;
import sn.oas.facturation.features.bonDeSortie.service.BonDeSortieService;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortieHistorique;

@Tag(name = "Bons de sortie", description = "Gestion des bons de sortie liés aux interventions")
@RestController
@RequestMapping("/api/bons-de-sortie")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
public class BonDeSortieController {

    private final BonDeSortieService bonDeSortieService;

    @Operation(summary = "Créer un bon de sortie", description = "Crée un bon en statut EN_ATTENTE et déplace uniquement les PDP du magasin vers l'atelier. Les PDG, PDS et mains-d'œuvre ne génèrent pas de mouvement de stock.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bon créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Client, véhicule ou pièces invalides")
    })
    @PostMapping("/creer")
    public ResponseEntity<BonDeSortieListResponse> creer(@RequestBody BonDeSortieRequest request) {
        return ResponseEntity.ok(BonDeSortieListResponse.from(bonDeSortieService.creer(request)));
    }

    @Operation(summary = "Valider un bon de sortie", description = "Après réparation, passe le bon en statut VALIDE et consomme uniquement les PDP sorties vers l'atelier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bon validé, stocks mis à jour"),
            @ApiResponse(responseCode = "400", description = "Bon déjà validé ou stock insuffisant pour une pièce")
    })
    @PutMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('SUPER_AGENT', 'MASTER', 'CHEF_ATELIER')")
    public ResponseEntity<BonDeSortieListResponse> valider(@PathVariable Long id) {
        return ResponseEntity.ok(BonDeSortieListResponse.from(bonDeSortieService.valider(id)));
    }

    @Operation(summary = "Obtenir un bon de sortie par ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bon trouvé"),
            @ApiResponse(responseCode = "400", description = "Bon introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BonDeSortieListResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(BonDeSortieListResponse.from(bonDeSortieService.getById(id)));
    }

    @Operation(summary = "Retourner une pièce d'un bon de sortie non validé", description = "Retire la pièce du bon de sortie et recrédite le stock magasin.")
    @PutMapping("/{id}/retour-piece/{pieceId}")
    public ResponseEntity<BonDeSortieListResponse> retournerPiece(@PathVariable Long id, @PathVariable Long pieceId) {
        return ResponseEntity.ok(BonDeSortieListResponse.from(bonDeSortieService.retournerPiece(id, pieceId)));
    }

    @Operation(summary = "Obtenir l'historique d'un bon de sortie", description = "Retourne la liste chronologique paginée des événements (SORTIE, SORTIE ATELIER, RETOUR) sur ce bon.")
    @GetMapping("/{id}/historique")
    public ResponseEntity<Page<BonDeSortieHistorique>> getHistorique(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bonDeSortieService.getHistorique(id, page, size));
    }

    @Operation(summary = "Obtenir l'historique global de tous les bons de sortie", description = "Retourne la liste chronologique paginée de tous les mouvements sur l'ensemble des bons de sortie.")
    @GetMapping("/historique-global")
    public ResponseEntity<Page<BonDeSortieHistoriqueListResponse>> getAllHistorique(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(bonDeSortieService.searchHistorique(keyword.trim(), page, size)
                    .map(BonDeSortieHistoriqueListResponse::from));
        }
        return ResponseEntity.ok(bonDeSortieService.getAllHistorique(page, size)
                .map(BonDeSortieHistoriqueListResponse::from));
    }

    @Operation(summary = "Lister les bons de sortie", description = "Retourne les bons de sortie avec pagination. Filtrable par mot-clé (keyword), statut (EN_ATTENTE/VALIDE), clientId ou vehiculeId.")
    @ApiResponse(responseCode = "200", description = "Page de bons de sortie retournée")
    @GetMapping
    public ResponseEntity<Page<BonDeSortieListResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) StatutBon statut,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehiculeId,
            @RequestParam(required = false) Long ordreReparationId,
            @RequestParam(required = false) Long ordreTravailId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(bonDeSortieService.search(keyword.trim(), page, size)
                    .map(BonDeSortieListResponse::from));
        }
        if (statut != null) {
            return ResponseEntity.ok(bonDeSortieService.getByStatut(statut, page, size)
                    .map(BonDeSortieListResponse::from));
        }
        if (clientId != null) {
            return ResponseEntity.ok(bonDeSortieService.getByClient(clientId, page, size)
                    .map(BonDeSortieListResponse::from));
        }
        if (vehiculeId != null) {
            return ResponseEntity.ok(bonDeSortieService.getByVehicule(vehiculeId, page, size)
                    .map(BonDeSortieListResponse::from));
        }
        if (ordreReparationId != null) {
            return ResponseEntity.ok(bonDeSortieService.getByOrdreReparation(ordreReparationId, page, size)
                    .map(BonDeSortieListResponse::from));
        }
        if (ordreTravailId != null) {
            return ResponseEntity.ok(bonDeSortieService.getByOrdreTravail(ordreTravailId, page, size)
                    .map(BonDeSortieListResponse::from));
        }
        return ResponseEntity.ok(bonDeSortieService.getAll(page, size)
                .map(BonDeSortieListResponse::from));
    }
}
