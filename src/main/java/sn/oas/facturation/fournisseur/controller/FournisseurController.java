package sn.oas.facturation.fournisseur.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.fournisseur.data.entity.Fournisseur;
import sn.oas.facturation.fournisseur.dto.FournisseurRequest;
import sn.oas.facturation.fournisseur.service.FournisseurService;

import java.util.List;

@RestController
@RequestMapping("/api/fournisseurs")
@RequiredArgsConstructor
public class FournisseurController {
    private final FournisseurService fournisseurService;

    @GetMapping
    public ResponseEntity<List<Fournisseur>> getFournisseurs(@RequestParam(required = false) String keyword){
        if (keyword != null && !keyword.trim().isEmpty()){
            return ResponseEntity.ok(fournisseurService.searchFournisseur(keyword));
        }
        return ResponseEntity.ok(fournisseurService.getAllFournisseur());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFournisseurById(@PathVariable Long id){
        try {
            return ResponseEntity.ok(fournisseurService.getFournisseurById(id));
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> addFournisseur(@RequestBody FournisseurRequest request){
        try {
            Fournisseur fournisseur = fournisseurService.createFournisseur(request);
            return ResponseEntity.ok(fournisseur);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFournisseur(@PathVariable Long id, @RequestBody FournisseurRequest request){
        try {
            Fournisseur fournisseur = fournisseurService.updateFournisseur(id,request);
            return ResponseEntity.ok(fournisseur);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFournisseur(@PathVariable Long id){
        try {
            fournisseurService.deleteFournisseur(id);
            return ResponseEntity.ok("Fournisseur supprimé avec succes");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<?> archiveFournisseur(@PathVariable Long id){
        try {
            fournisseurService.archiveFournisseur(id);
            return ResponseEntity.ok("Fournisseur archivé avec succes");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/unarchive")
    public ResponseEntity<?> unarchiveFournisseur(@PathVariable Long id){
        try {
            fournisseurService.unarchiveFournisseur(id);
            return ResponseEntity.ok("Fournisseur désararchivé avec succes");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
