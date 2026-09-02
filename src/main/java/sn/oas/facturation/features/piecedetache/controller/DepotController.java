package sn.oas.facturation.features.piecedetache.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.piecedetache.data.entity.Depot;
import sn.oas.facturation.features.piecedetache.repository.DepotRepository;
import java.util.List;
@RestController
@RequestMapping("/api/depots")
public class DepotController {
    private final DepotRepository repository;
    public DepotController(DepotRepository repository) { this.repository = repository; }
    @GetMapping
    public ResponseEntity<List<Depot>> getAll() { return ResponseEntity.ok(repository.findAll()); }
    @PostMapping
    public ResponseEntity<Depot> create(@RequestBody Depot entity) { return ResponseEntity.ok(repository.save(entity)); }
    @PutMapping("/{id}")
    public ResponseEntity<Depot> update(@PathVariable Long id, @RequestBody Depot entity) {
        return repository.findById(id).map(existing -> {
            existing.setNom(entity.getNom());
            existing.setDescription(entity.getDescription());
            return ResponseEntity.ok(repository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
