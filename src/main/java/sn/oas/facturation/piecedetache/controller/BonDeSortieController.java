package sn.oas.facturation.piecedetache.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.piecedetache.data.entity.BonDeSortie;
import sn.oas.facturation.piecedetache.data.enums.StatutBon;
import sn.oas.facturation.piecedetache.dto.BonDeSortieRequest;
import sn.oas.facturation.piecedetache.service.BonDeSortieService;

import java.util.List;

@Tag(name = "Bons de sortie", description = "Gestion des bons de sortie liés aux interventions")
@RestController
@RequestMapping("/api/bons-de-sortie")
@RequiredArgsConstructor
public class BonDeSortieController {

    private final BonDeSortieService bonDeSortieService;

    @Operation(summary = "Créer un bon de sortie", description = "Crée un bon en statut EN_ATTENTE. La référence est générée automatiquement (ex: BS-2026-0001). Aucun mouvement de stock n'est effectué à cette étape.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bon créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Client, véhicule ou pièces invalides")
    })
    @PostMapping("/creer")
    public ResponseEntity<BonDeSortie> creer(@RequestBody BonDeSortieRequest request) {
        return ResponseEntity.ok(bonDeSortieService.creer(request));
    }

    @Operation(summary = "Valider un bon de sortie", description = "Passe le bon en statut VALIDE. Vérifie le stock magasin pour chaque pièce et génère les mouvements de sortie (magasin → atelier).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bon validé, stocks mis à jour"),
            @ApiResponse(responseCode = "400", description = "Bon déjà validé ou stock insuffisant pour une pièce")
    })
    @PutMapping("/{id}/valider")
    public ResponseEntity<BonDeSortie> valider(@PathVariable Long id) {
        return ResponseEntity.ok(bonDeSortieService.valider(id));
    }

    @Operation(summary = "Obtenir un bon de sortie par ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bon trouvé"),
            @ApiResponse(responseCode = "400", description = "Bon introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BonDeSortie> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bonDeSortieService.getById(id));
    }

    @Operation(summary = "Lister les bons de sortie", description = "Retourne tous les bons. Filtrable par statut (EN_ATTENTE/VALIDE), clientId ou vehiculeId.")
    @ApiResponse(responseCode = "200", description = "Liste retournée")
    @GetMapping
    public ResponseEntity<List<BonDeSortie>> getAll(
            @RequestParam(required = false) StatutBon statut,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehiculeId) {
        if (statut != null) return ResponseEntity.ok(bonDeSortieService.getByStatut(statut));
        if (clientId != null) return ResponseEntity.ok(bonDeSortieService.getByClient(clientId));
        if (vehiculeId != null) return ResponseEntity.ok(bonDeSortieService.getByVehicule(vehiculeId));
        return ResponseEntity.ok(bonDeSortieService.getAll());
    }
}