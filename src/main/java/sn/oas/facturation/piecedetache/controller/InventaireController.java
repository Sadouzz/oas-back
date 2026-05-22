package sn.oas.facturation.piecedetache.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.piecedetache.dto.InventaireRequest;
import sn.oas.facturation.piecedetache.dto.InventaireResponse;
import sn.oas.facturation.piecedetache.service.InventaireService;

@RestController
@RequestMapping("/api/inventaire")
@RequiredArgsConstructor
public class InventaireController {

    private final InventaireService inventaireService;

    @PostMapping("/compter")
    public ResponseEntity<InventaireResponse> compterPiece(@RequestBody InventaireRequest request) {
        return ResponseEntity.ok(inventaireService.compterPiece(request));
    }
}