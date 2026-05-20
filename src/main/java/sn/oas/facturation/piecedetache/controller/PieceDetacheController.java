package sn.oas.facturation.piecedetache.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.piecedetache.data.enums.StatutPiece;
import sn.oas.facturation.piecedetache.data.enums.TypePiece;
import sn.oas.facturation.piecedetache.dto.PieceDetacheRequest;
import sn.oas.facturation.piecedetache.service.PieceDetacheService;

import java.util.List;

@RestController
@RequestMapping("/api/pieces-detachees")
@RequiredArgsConstructor
public class PieceDetacheController {

    private final PieceDetacheService pieceDetacheService;

    @GetMapping
    public ResponseEntity<List<PieceDetache>> list(
            @RequestParam(required = false) StatutPiece statut,
            @RequestParam(required = false) TypePiece type,
            @RequestParam(required = false) String keyword) {

        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(pieceDetacheService.search(keyword.trim()));
        }
        if (statut != null) {
            return ResponseEntity.ok(pieceDetacheService.filterByStatut(statut));
        }
        if (type != null) {
            return ResponseEntity.ok(pieceDetacheService.filterByType(type));
        }
        return ResponseEntity.ok(pieceDetacheService.getAllPieces());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(pieceDetacheService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody PieceDetacheRequest request) {
        try {
            return ResponseEntity.ok(pieceDetacheService.create(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PieceDetacheRequest request) {
        try {
            return ResponseEntity.ok(pieceDetacheService.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            pieceDetacheService.delete(id);
            return ResponseEntity.ok("Pièce détachée supprimée avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
