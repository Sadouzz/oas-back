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
import sn.oas.facturation.features.piecedetache.data.entity.StockMouvement;
import sn.oas.facturation.features.piecedetache.data.enums.TypeMouvement;
import sn.oas.facturation.features.piecedetache.dto.AjustementStockRequest;
import sn.oas.facturation.features.piecedetache.dto.AlerteStockResponse;
import sn.oas.facturation.features.piecedetache.dto.EntreeStockRequest;
import sn.oas.facturation.features.piecedetache.dto.SortieStockRequest;
import sn.oas.facturation.features.piecedetache.dto.StockMouvementListResponse;
import sn.oas.facturation.features.piecedetache.service.AlerteService;
import sn.oas.facturation.features.piecedetache.service.StockService;

import java.time.LocalDateTime;

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

    @Operation(summary = "Historique des mouvements d'une pièce", description = "Retourne tous les mouvements d'une PDP avec pagination, filtrables par type (ENTREE, SORTIE, AJUSTEMENT, INVENTAIRE).")
    @ApiResponse(responseCode = "200", description = "Historique retourné")
    @GetMapping("/historique/{pieceId}")
    public ResponseEntity<Page<StockMouvementListResponse>> historiquePiece(
            @PathVariable Long pieceId,
            @RequestParam(required = false) TypeMouvement type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (type != null) {
            return ResponseEntity.ok(stockService.getHistoriquePieceByType(pieceId, type, page, size).map(StockMouvementListResponse::from));
        }
        return ResponseEntity.ok(stockService.getHistoriquePiece(pieceId, page, size).map(StockMouvementListResponse::from));
    }

    @Operation(summary = "Historique global des mouvements", description = "Retourne tous les mouvements de stock sur une période donnée avec pagination, avec possibilité de filtrer par pièce, catégorie ou type de mouvement.")
    @ApiResponse(responseCode = "200", description = "Historique retourné")
    @GetMapping("/historique")
    public ResponseEntity<Page<StockMouvementListResponse>> historiqueGlobal(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @RequestParam(required = false) Long pieceId,
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) TypeMouvement type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        LocalDateTime d = debut != null ? debut : LocalDateTime.now().minusYears(5);
        LocalDateTime f = fin != null ? fin : LocalDateTime.now().plusDays(1);
        return ResponseEntity.ok(stockService.getHistoriqueGlobal(d, f, pieceId, categorie, type, page, size).map(StockMouvementListResponse::from));
    }

    @Operation(summary = "Toutes les alertes", description = "Retourne les PDP en rupture de stock (stockMagasin = 0) et en stock faible (stockMagasin ≤ seuil) avec pagination.")
    @ApiResponse(responseCode = "200", description = "Alertes retournées")
    @GetMapping("/alertes")
    public ResponseEntity<Page<AlerteStockResponse>> alertes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(alerteService.getAlertes(page, size));
    }

    @Operation(summary = "Ruptures de stock", description = "Retourne uniquement les PDP dont le stock magasin est à 0 avec pagination.")
    @ApiResponse(responseCode = "200", description = "Ruptures retournées")
    @GetMapping("/alertes/ruptures")
    public ResponseEntity<Page<AlerteStockResponse>> ruptures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(alerteService.getRuptures(page, size));
    }

    @Operation(summary = "Stocks faibles", description = "Retourne les PDP dont le stock magasin est inférieur ou égal au seuil minimum (par pièce ou global = 10) avec pagination.")
    @ApiResponse(responseCode = "200", description = "Stocks faibles retournés")
    @GetMapping("/alertes/stocks-faibles")
    public ResponseEntity<Page<AlerteStockResponse>> stocksFaibles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(alerteService.getStocksFaibles(page, size));
    }
}
