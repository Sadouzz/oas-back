package sn.oas.facturation.features.piecedetache.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.piecedetache.data.entity.PieceMouvement;
import sn.oas.facturation.features.piecedetache.dto.AjustementStockRequest;
import sn.oas.facturation.features.piecedetache.dto.EntreeStockRequest;
import sn.oas.facturation.features.piecedetache.dto.SortieStockRequest;
import sn.oas.facturation.features.piecedetache.service.StockService;

@Tag(name = "Gestion du stock", description = "Entrées, sorties et ajustements de stock des PDP")
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @Operation(summary = "Entrée de stock", description = "Ajoute une quantité au stock magasin d'une PDP. La qteReelle est recalculée automatiquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrée enregistrée"),
            @ApiResponse(responseCode = "400", description = "Pièce introuvable ou quantité invalide")
    })
    @PostMapping("/entree")
    public ResponseEntity<PieceMouvement> entree(@RequestBody EntreeStockRequest request) {
        return ResponseEntity.ok(stockService.entree(request));
    }

    @Operation(summary = "Sortie de stock", description = "Transfère une quantité du stock magasin vers le stock atelier d'une PDP.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sortie enregistrée"),
            @ApiResponse(responseCode = "400", description = "Stock magasin insuffisant ou quantité invalide")
    })
    @PostMapping("/sortie")
    public ResponseEntity<PieceMouvement> sortie(@RequestBody SortieStockRequest request) {
        return ResponseEntity.ok(stockService.sortie(request));
    }

    @Operation(summary = "Ajustement de stock", description = "Corrige manuellement le stock magasin et atelier d'une PDP. La qteReelle est recalculée.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ajustement enregistré"),
            @ApiResponse(responseCode = "400", description = "Valeurs négatives ou pièce introuvable")
    })
    @PostMapping("/ajustement")
    public ResponseEntity<PieceMouvement> ajustement(@RequestBody AjustementStockRequest request) {
        return ResponseEntity.ok(stockService.ajustement(request));
    }
}
