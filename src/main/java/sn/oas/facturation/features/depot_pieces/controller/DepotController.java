package sn.oas.facturation.features.depot_pieces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.depot_pieces.data.entity.Depot;
import sn.oas.facturation.features.depot_pieces.dto.request.DepotRequest;
import sn.oas.facturation.features.depot_pieces.dto.response.DepotResponse;
import sn.oas.facturation.features.depot_pieces.service.DepotService;

import java.util.List;

@RestController
@RequestMapping("/api/depots")
@RequiredArgsConstructor
@CrossOrigin("*")
@Tag(name = "Dépôts de pièces", description = "API de gestion des dépôts de pièces détachées")
public class DepotController {

    private final DepotService depotService;

    @GetMapping
    @Operation(summary = "Récupérer la liste des dépôts (paginée ou complète)")
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                Page<DepotResponse> result = depotService.searchDepots(keyword.trim(), page, size)
                        .map(DepotResponse::from);
                return ResponseEntity.ok(result);
            }
            Page<DepotResponse> result = depotService.getAllDepots(page, size)
                    .map(DepotResponse::from);
            return ResponseEntity.ok(result);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<DepotResponse> list = depotService.searchDepots(keyword.trim()).stream()
                    .map(DepotResponse::from)
                    .toList();
            return ResponseEntity.ok(list);
        }
        List<DepotResponse> list = depotService.getAllDepots().stream()
                .map(DepotResponse::from)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un dépôt par son ID")
    public ResponseEntity<DepotResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(DepotResponse.from(depotService.getDepotById(id)));
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau dépôt")
    public ResponseEntity<DepotResponse> create(@Valid @RequestBody DepotRequest request) {
        Depot created = depotService.createDepot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(DepotResponse.from(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un dépôt")
    public ResponseEntity<DepotResponse> update(@PathVariable Long id, @Valid @RequestBody DepotRequest request) {
        Depot updated = depotService.updateDepot(id, request);
        return ResponseEntity.ok(DepotResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un dépôt")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        depotService.deleteDepot(id);
        return ResponseEntity.noContent().build();
    }
}
