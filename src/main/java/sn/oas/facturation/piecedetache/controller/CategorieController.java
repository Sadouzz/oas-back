package sn.oas.facturation.piecedetache.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.piecedetache.data.entity.Categorie;
import sn.oas.facturation.piecedetache.repository.CategorieRepository;
import java.util.List;
@RestController
@RequestMapping("/api/categories")
public class CategorieController {
    private final CategorieRepository repository;
    public CategorieController(CategorieRepository repository) { this.repository = repository; }
    @GetMapping
    public ResponseEntity<List<Categorie>> getAll() { return ResponseEntity.ok(repository.findAll()); }
    @PostMapping
    public ResponseEntity<Categorie> create(@RequestBody Categorie entity) { return ResponseEntity.ok(repository.save(entity)); }
    @PutMapping("/{id}")
    public ResponseEntity<Categorie> update(@PathVariable Long id, @RequestBody Categorie entity) {
        return repository.findById(id).map(existing -> {
            existing.setNom(entity.getNom());
            existing.setDepot(entity.getDepot());
            return ResponseEntity.ok(repository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
