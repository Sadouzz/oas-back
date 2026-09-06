package sn.oas.facturation.features.piecedetache.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.data.enums.TypeMouvement;
import sn.oas.facturation.features.piecedetache.data.enums.TypePiece;
import sn.oas.facturation.features.piecedetache.dto.PieceDetacheListResponse;
import sn.oas.facturation.features.piecedetache.dto.PieceDetacheRequest;
import sn.oas.facturation.features.piecedetache.dto.PieceMouvementListResponse;
import sn.oas.facturation.features.piecedetache.service.PieceDetacheService;
import sn.oas.facturation.features.piecedetache.service.StockService;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Pièces détachées", description = "CRUD des pièces détachées (PDP, PDG, PDS) et historique des mouvements")
@RestController
@RequestMapping("/api/pieces-detachees")
@RequiredArgsConstructor
public class PieceDetacheController {

    private final PieceDetacheService pieceDetacheService;
    private final StockService stockService;

    @Operation(summary = "Lister les pièces", description = "Retourne toutes les pièces. Filtrable par type ou mot-clé avec pagination.")
    @ApiResponse(responseCode = "200", description = "Liste retournée avec succès")
    @GetMapping
    public ResponseEntity<Page<PieceDetacheListResponse>> list(
            @RequestParam(required = false) TypePiece type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(pieceDetacheService.searchPieces(keyword.trim(), page, size).map(PieceDetacheListResponse::from));
        }
        if (type != null) {
            return ResponseEntity.ok(pieceDetacheService.filterByType(type, page, size).map(PieceDetacheListResponse::from));
        }
        return ResponseEntity.ok(pieceDetacheService.getAllPieces(page, size).map(PieceDetacheListResponse::from));
    }

    @Operation(summary = "Obtenir une pièce par ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pièce trouvée"),
            @ApiResponse(responseCode = "400", description = "Pièce introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(pieceDetacheService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Créer une pièce détachée", description = "Crée une PDP, PDG ou PDS. Le stock magasin et le prix sont obligatoires pour une PDP.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pièce créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou numéro de série déjà existant")
    })
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody PieceDetacheRequest request) {
        try {
            return ResponseEntity.ok(pieceDetacheService.create(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Mettre à jour une pièce", description = "Met à jour les informations d'une pièce. Les stocks ne sont pas modifiables via cet endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pièce mise à jour"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou pièce introuvable")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PieceDetacheRequest request) {
        try {
            return ResponseEntity.ok(pieceDetacheService.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Supprimer une pièce détachée")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pièce supprimée"),
            @ApiResponse(responseCode = "400", description = "Pièce introuvable")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            pieceDetacheService.delete(id);
            return ResponseEntity.ok(java.util.Map.of("message", "Pièce détachée supprimée avec succès !"));
        } catch (RuntimeException e) {
            e.printStackTrace(); // Pour voir l'erreur dans la console backend
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @Operation(summary = "Restaurer une pièce archivée")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pièce restaurée"),
            @ApiResponse(responseCode = "400", description = "Pièce introuvable")
    })
    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restore(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(pieceDetacheService.restore(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Historique des mouvements d'une pièce", description = "Retourne tous les mouvements d'une PDP avec pagination, filtrables par type (ENTREE, SORTIE, AJUSTEMENT, INVENTAIRE).")
    @ApiResponse(responseCode = "200", description = "Historique retourné")
    @GetMapping({"/{pieceId}/historique", "/historique/{pieceId}"})
    public ResponseEntity<Page<PieceMouvementListResponse>> historiquePiece(
            @PathVariable Long pieceId,
            @RequestParam(required = false) TypeMouvement type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (type != null) {
            return ResponseEntity.ok(stockService.getHistoriquePieceByType(pieceId, type, page, size).map(PieceMouvementListResponse::from));
        }
        return ResponseEntity.ok(stockService.getHistoriquePiece(pieceId, page, size).map(PieceMouvementListResponse::from));
    }

    @Operation(summary = "Historique global des mouvements", description = "Retourne tous les mouvements de stock sur une période donnée avec pagination, avec possibilité de filtrer par mot-clé, pièce, catégorie ou type de mouvement.")
    @ApiResponse(responseCode = "200", description = "Historique retourné")
    @GetMapping("/historique")
    public ResponseEntity<Page<PieceMouvementListResponse>> historiqueGlobal(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @RequestParam(required = false) Long pieceId,
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) TypeMouvement type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(stockService.getHistoriqueGlobal(keyword, debut, fin, pieceId, categorie, type, page, size).map(PieceMouvementListResponse::from));
    }
}
