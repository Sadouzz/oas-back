package sn.oas.facturation.partenaire.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.partenaire.dto.PartenaireRequest;
import sn.oas.facturation.partenaire.dto.PartenaireResponse;
import sn.oas.facturation.partenaire.service.PartenaireService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/partenaires")
@RequiredArgsConstructor
public class PartenaireController {

    private final PartenaireService partenaireService;

    @GetMapping
    public ResponseEntity<List<PartenaireResponse>> getAll() {
        return ResponseEntity.ok(partenaireService.getAllPartenaires());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartenaireResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(partenaireService.getPartenaireById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<PartenaireResponse> create(@RequestBody PartenaireRequest request) {
        return ResponseEntity.ok(partenaireService.createPartenaire(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartenaireResponse> update(@PathVariable Long id, @RequestBody PartenaireRequest request) {
        return ResponseEntity.ok(partenaireService.updatePartenaire(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        partenaireService.deletePartenaire(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<PartenaireResponse> archive(@PathVariable Long id) {
        return ResponseEntity.ok(partenaireService.archivePartenaire(id));
    }

    @PatchMapping("/{id}/unarchive")
    public ResponseEntity<PartenaireResponse> unarchive(@PathVariable Long id) {
        return ResponseEntity.ok(partenaireService.unarchivePartenaire(id));
    }
}
