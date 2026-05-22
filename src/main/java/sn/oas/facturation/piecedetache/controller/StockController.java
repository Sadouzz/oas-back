package sn.oas.facturation.piecedetache.controller;

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

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final AlerteService alerteService;

    @PostMapping("/entree")
    public ResponseEntity<StockMouvement> entree(@RequestBody EntreeStockRequest request) {
        return ResponseEntity.ok(stockService.entree(request));
    }

    @PostMapping("/sortie")
    public ResponseEntity<StockMouvement> sortie(@RequestBody SortieStockRequest request) {
        return ResponseEntity.ok(stockService.sortie(request));
    }

    @PostMapping("/ajustement")
    public ResponseEntity<StockMouvement> ajustement(@RequestBody AjustementStockRequest request) {
        return ResponseEntity.ok(stockService.ajustement(request));
    }

    @GetMapping("/historique/{pieceId}")
    public ResponseEntity<List<StockMouvement>> historiquePiece(
            @PathVariable Long pieceId,
            @RequestParam(required = false) TypeMouvement type) {
        if (type != null) {
            return ResponseEntity.ok(stockService.getHistoriquePieceByType(pieceId, type));
        }
        return ResponseEntity.ok(stockService.getHistoriquePiece(pieceId));
    }

    @GetMapping("/historique")
    public ResponseEntity<List<StockMouvement>> historiqueGlobal(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(stockService.getHistoriqueGlobal(debut, fin));
    }

    @GetMapping("/alertes")
    public ResponseEntity<List<AlerteStockResponse>> alertes() {
        return ResponseEntity.ok(alerteService.getAlertes());
    }

    @GetMapping("/alertes/ruptures")
    public ResponseEntity<List<AlerteStockResponse>> ruptures() {
        return ResponseEntity.ok(alerteService.getRuptures());
    }

    @GetMapping("/alertes/stocks-faibles")
    public ResponseEntity<List<AlerteStockResponse>> stocksFaibles() {
        return ResponseEntity.ok(alerteService.getStocksFaibles());
    }
}
