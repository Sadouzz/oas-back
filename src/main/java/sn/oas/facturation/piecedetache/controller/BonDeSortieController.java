package sn.oas.facturation.piecedetache.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.piecedetache.data.entity.BonDeSortie;
import sn.oas.facturation.piecedetache.data.enums.StatutBon;
import sn.oas.facturation.piecedetache.dto.BonDeSortieRequest;
import sn.oas.facturation.piecedetache.service.BonDeSortieService;

import java.util.List;

@RestController
@RequestMapping("/api/bons-de-sortie")
@RequiredArgsConstructor
public class BonDeSortieController {

    private final BonDeSortieService bonDeSortieService;

    @PostMapping("/creer")
    public ResponseEntity<BonDeSortie> creer(@RequestBody BonDeSortieRequest request) {
        return ResponseEntity.ok(bonDeSortieService.creer(request));
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<BonDeSortie> valider(@PathVariable Long id) {
        return ResponseEntity.ok(bonDeSortieService.valider(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BonDeSortie> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bonDeSortieService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<BonDeSortie>> getAll(
            @RequestParam(required = false) StatutBon statut,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehiculeId) {
        if (statut != null) return ResponseEntity.ok(bonDeSortieService.getByStatut(statut));
        if (clientId != null) return ResponseEntity.ok(bonDeSortieService.getByClient(clientId));
        if (vehiculeId != null) return ResponseEntity.ok(bonDeSortieService.getByVehicule(vehiculeId));
        return ResponseEntity.ok(bonDeSortieService.getAll());
    }
}