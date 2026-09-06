package sn.oas.facturation.features.piecedetache.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.oas.facturation.features.piecedetache.dto.AlerteStockResponse;
import sn.oas.facturation.features.piecedetache.service.AlerteService;

import java.util.List;

@Tag(name = "Alertes de stock", description = "Consultation des alertes de stock (ruptures et stocks faibles des PDP)")
@RestController
@RequestMapping("/api/alertes")
@RequiredArgsConstructor
public class AlerteController {

    private final AlerteService alerteService;

    @Operation(summary = "Toutes les alertes (paginées)", description = "Retourne les PDP en rupture de stock (stockMagasin = 0) et en stock faible (stockMagasin ≤ seuil) avec pagination.")
    @ApiResponse(responseCode = "200", description = "Alertes retournées avec succès")
    @GetMapping
    public ResponseEntity<Page<AlerteStockResponse>> getAlertes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(alerteService.getAlertes(page, size));
    }

    @Operation(summary = "Ruptures de stock (paginées)", description = "Retourne uniquement les PDP dont le stock magasin est à 0 avec pagination.")
    @ApiResponse(responseCode = "200", description = "Ruptures retournées avec succès")
    @GetMapping("/ruptures")
    public ResponseEntity<Page<AlerteStockResponse>> getRuptures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(alerteService.getRuptures(page, size));
    }

    @Operation(summary = "Stocks faibles (paginés)", description = "Retourne les PDP dont le stock magasin est inférieur ou égal au seuil minimum avec pagination.")
    @ApiResponse(responseCode = "200", description = "Stocks faibles retournés avec succès")
    @GetMapping("/stocks-faibles")
    public ResponseEntity<Page<AlerteStockResponse>> getStocksFaibles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(alerteService.getStocksFaibles(page, size));
    }

}
