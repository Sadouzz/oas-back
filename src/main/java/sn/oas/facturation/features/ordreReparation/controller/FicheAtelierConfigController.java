package sn.oas.facturation.features.ordreReparation.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.ordreReparation.data.entity.FicheAtelierConfig;
import sn.oas.facturation.features.ordreReparation.repository.FicheAtelierConfigRepository;
import java.util.List;
@RestController
@RequestMapping("/api/fiche-atelier-configs")
public class FicheAtelierConfigController {
    private final FicheAtelierConfigRepository repository;
    public FicheAtelierConfigController(FicheAtelierConfigRepository repository) { this.repository = repository; }
    @GetMapping
    public ResponseEntity<List<FicheAtelierConfig>> getAll() { return ResponseEntity.ok(repository.findAll()); }
    @PostMapping
    public ResponseEntity<FicheAtelierConfig> create(@RequestBody FicheAtelierConfig entity) { return ResponseEntity.ok(repository.save(entity)); }
    @PutMapping("/{id}")
    public ResponseEntity<FicheAtelierConfig> update(@PathVariable Long id, @RequestBody FicheAtelierConfig entity) {
        return repository.findById(id).map(existing -> {
            existing.setConfigJson(entity.getConfigJson());
            return ResponseEntity.ok(repository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
