package sn.oas.facturation.features.piecedetache.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;

import sn.oas.facturation.features.piecedetache.data.enums.TypePiece;
import sn.oas.facturation.features.piecedetache.dto.PieceDetacheRequest;
import sn.oas.facturation.features.piecedetache.service.PieceDetacheService;

import java.util.List;

@Tag(name = "Pièces détachées", description = "CRUD des pièces détachées (PDP, PDG, PDS)")
@RestController
@RequestMapping("/api/pieces-detachees")
@RequiredArgsConstructor
public class PieceDetacheController {

    private final PieceDetacheService pieceDetacheService;

    @Operation(summary = "Lister les pièces", description = "Retourne toutes les pièces. Filtrable par type ou mot-clé avec pagination.")
    @ApiResponse(responseCode = "200", description = "Liste retournée avec succès")
    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) TypePiece type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(pieceDetacheService.searchPieces(keyword.trim()));
        }
        if (type != null) {
            return ResponseEntity.ok(pieceDetacheService.filterByType(type));
        }
        return ResponseEntity.ok(pieceDetacheService.getAllPieces(page, size));
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
}
