package sn.oas.facturation.main_doeuvre.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.main_doeuvre.dto.MainDoeuvreRequest;
import sn.oas.facturation.main_doeuvre.service.MainDoeuvreService;
import java.util.List;

@RestController
@RequestMapping("/api/main-doeuvres")
@RequiredArgsConstructor
public class MainDoeuvreController {

    private final MainDoeuvreService mainDoeuvreService;

    @GetMapping
    public ResponseEntity<List<MainDoeuvre>> getAllMainDoeuvres() {
        return ResponseEntity.ok(mainDoeuvreService.getAllMainDoeuvres());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MainDoeuvre> getMainDoeuvreById(@PathVariable Long id) {
        return ResponseEntity.ok(mainDoeuvreService.getMainDoeuvreById(id));
    }

    @PostMapping
    public ResponseEntity<MainDoeuvre> createMainDoeuvre(@RequestBody MainDoeuvreRequest request) {
        MainDoeuvre created = mainDoeuvreService.createMainDoeuvre(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MainDoeuvre> updateMainDoeuvre(@PathVariable Long id,
            @RequestBody MainDoeuvreRequest request) {
        return ResponseEntity.ok(mainDoeuvreService.updateMainDoeuvre(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMainDoeuvre(@PathVariable Long id) {
        mainDoeuvreService.deleteMainDoeuvre(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<MainDoeuvre> archiveMainDoeuvre(@PathVariable Long id,
        @RequestParam boolean archived) {
        return ResponseEntity.ok(mainDoeuvreService.archiveMainDoeuvre(id, archived));
    }
}
