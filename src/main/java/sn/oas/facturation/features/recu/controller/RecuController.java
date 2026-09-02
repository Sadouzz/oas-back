package sn.oas.facturation.features.recu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sn.oas.facturation.features.recu.dto.RecuRequest;
import sn.oas.facturation.features.recu.dto.RecuResponse;
import sn.oas.facturation.features.recu.service.RecuService;

import java.util.List;

@RestController
@RequestMapping("/api/recus")
@RequiredArgsConstructor
@Tag(name = "Reçus", description = "API pour la gestion des reçus de paiement de facture")
public class RecuController {

    private final RecuService recuService;

    @PostMapping
    @Operation(summary = "Créer un nouveau reçu pour une facture")
    public ResponseEntity<RecuResponse> create(@RequestBody RecuRequest request) {
        return ResponseEntity.ok(recuService.create(request));
    }

    @GetMapping("/facture/{factureId}")
    @Operation(summary = "Récupérer les reçus d'une facture spécifique")
    public ResponseEntity<List<RecuResponse>> getByFacture(@PathVariable Long factureId) {
        return ResponseEntity.ok(recuService.getByFacture(factureId));
    }
}
