package sn.oas.facturation.features.piecedetache.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.piecedetache.dto.InventaireRequest;
import sn.oas.facturation.features.piecedetache.dto.InventaireResponse;
import sn.oas.facturation.features.piecedetache.service.InventaireService;

@Tag(name = "Inventaire", description = "Comptage physique des pièces et réconciliation avec le stock théorique")
@RestController
@RequestMapping("/api/inventaire")
@RequiredArgsConstructor
public class InventaireController {

    private final InventaireService inventaireService;

    @Operation(
            summary = "Compter une pièce",
            description = "Compare le stock physique saisi avec le stock théorique. " +
                    "Si un écart est détecté, le stock est mis à jour et un mouvement INVENTAIRE est enregistré. " +
                    "Si aucun écart, aucune modification n'est effectuée."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comptage effectué. Le champ 'ajuste' indique si un écart a été corrigé."),
            @ApiResponse(responseCode = "400", description = "Pièce introuvable ou valeurs négatives")
    })
    @PostMapping("/compter")
    public ResponseEntity<InventaireResponse> compterPiece(@RequestBody InventaireRequest request) {
        return ResponseEntity.ok(inventaireService.compterPiece(request));
    }
}
