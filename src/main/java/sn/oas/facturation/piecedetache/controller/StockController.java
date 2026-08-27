package sn.oas.facturation.piecedetache.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.piecedetache.data.entity.StockMouvement;
import sn.oas.facturation.piecedetache.data.enums.TypeMouvement;
import sn.oas.facturation.piecedetache.dto.AjustementStockRequest;
import sn.oas.facturation.piecedetache.dto.AlerteStockResponse;
import sn.oas.facturation.piecedetache.dto.EntreeStockRequest;
import sn.oas.facturation.piecedetache.dto.SortieStockRequest;
import sn.oas.facturation.piecedetache.service.AlerteService;
import sn.oas.facturation.piecedetache.service.StockService;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Gestion du stock", description = "Entrées, sorties, ajustements et alertes de stock des PDP")
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final AlerteService alerteService;

    @Operation(summary = "Entrée de stock", description = "Ajoute une quantité au stock magasin d'une PDP. La qteReelle est recalculée automatiquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrée enregistrée"),
            @ApiResponse(responseCode = "400", description = "Pièce introuvable ou quantité invalide")
    })
    @PostMapping("/entree")
    public ResponseEntity<StockMouvement> entree(@RequestBody EntreeStockRequest request) {
        return ResponseEntity.ok(stockService.entree(request));
    }

    @Operation(summary = "Sortie de stock", description = "Transfère une quantité du stock magasin vers le stock atelier d'une PDP.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sortie enregistrée"),
            @ApiResponse(responseCode = "400", description = "Stock magasin insuffisant ou quantité invalide")
    })
    @PostMapping("/sortie")
    public ResponseEntity<StockMouvement> sortie(@RequestBody SortieStockRequest request) {
        return ResponseEntity.ok(stockService.sortie(request));
    }

    @Operation(summary = "Ajustement de stock", description = "Corrige manuellement le stock magasin et atelier d'une PDP. La qteReelle est recalculée.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ajustement enregistré"),
            @ApiResponse(responseCode = "400", description = "Valeurs négatives ou pièce introuvable")
    })
    @PostMapping("/ajustement")
    public ResponseEntity<StockMouvement> ajustement(@RequestBody AjustementStockRequest request) {
        return ResponseEntity.ok(stockService.ajustement(request));
    }

    @Operation(summary = "Historique des mouvements d'une pièce", description = "Retourne tous les mouvements d'une PDP, filtrables par type (ENTREE, SORTIE, AJUSTEMENT, INVENTAIRE).")
    @ApiResponse(responseCode = "200", description = "Historique retourné")
    @GetMapping("/historique/{pieceId}")
    public ResponseEntity<List<StockMouvement>> historiquePiece(
            @PathVariable Long pieceId,
            @RequestParam(required = false) TypeMouvement type) {
        if (type != null) {
            return ResponseEntity.ok(stockService.getHistoriquePieceByType(pieceId, type));
        }
        return ResponseEntity.ok(stockService.getHistoriquePiece(pieceId));
    }

    @Operation(summary = "Historique global des mouvements", description = "Retourne tous les mouvements de stock sur une période donnée (format ISO : 2026-01-01T00:00:00), avec possibilité de filtrer par pièce, catégorie ou type de mouvement.")
    @ApiResponse(responseCode = "200", description = "Historique retourné")
    @GetMapping("/historique")
    public ResponseEntity<List<StockMouvement>> historiqueGlobal(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @RequestParam(required = false) Long pieceId,
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) TypeMouvement type) {
        return ResponseEntity.ok(stockService.getHistoriqueGlobal(debut, fin, pieceId, categorie, type));
    }

    @Operation(summary = "Toutes les alertes", description = "Retourne les PDP en rupture de stock (stockMagasin = 0) et en stock faible (stockMagasin ≤ seuil).")
    @ApiResponse(responseCode = "200", description = "Alertes retournées")
    @GetMapping("/alertes")
    public ResponseEntity<List<AlerteStockResponse>> alertes() {
        return ResponseEntity.ok(alerteService.getAlertes());
    }

    @Operation(summary = "Ruptures de stock", description = "Retourne uniquement les PDP dont le stock magasin est à 0.")
    @ApiResponse(responseCode = "200", description = "Ruptures retournées")
    @GetMapping("/alertes/ruptures")
    public ResponseEntity<List<AlerteStockResponse>> ruptures() {
        return ResponseEntity.ok(alerteService.getRuptures());
    }

    @Operation(summary = "Stocks faibles", description = "Retourne les PDP dont le stock magasin est inférieur ou égal au seuil minimum (par pièce ou global = 10).")
    @ApiResponse(responseCode = "200", description = "Stocks faibles retournés")
    @GetMapping("/alertes/stocks-faibles")
    public ResponseEntity<List<AlerteStockResponse>> stocksFaibles() {
        return ResponseEntity.ok(alerteService.getStocksFaibles());
    }
}
