package sn.oas.facturation.features.bonDeLivraison.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonCreateRequest;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonResponse;
import sn.oas.facturation.features.bonDeLivraison.service.BonDeLivraisonService;
import sn.oas.facturation.features.facture.dto.FactureResponse;
import sn.oas.facturation.features.noteDePrix.dto.NoteDePrixResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bons-de-livraison")
@RequiredArgsConstructor
public class BonDeLivraisonController {
    private final BonDeLivraisonService service;

    @PostMapping
    public ResponseEntity<BonDeLivraisonResponse> create(@Valid @RequestBody BonDeLivraisonCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @GetMapping
    public Page<BonDeLivraisonResponse> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String keyword) {
        return service.list(page, size, keyword);
    }
    @GetMapping("/{id}")
    public BonDeLivraisonResponse get(@PathVariable Long id) { return service.get(id); }
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=BL_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF).body(service.generatePdf(id));
    }
    @GetMapping("/ordres-reparation/{ordreId}/bons-de-sortie")
    public List<Map<String, Object>> bonsDeSortie(@PathVariable Long ordreId) { return service.bonsDeSortieDisponibles(ordreId); }
    @PostMapping("/{id}/convertir-en-facture")
    public FactureResponse convertirEnFacture(@PathVariable Long id) { return FactureResponse.from(service.convertirEnFacture(id)); }
    @PostMapping("/{id}/convertir-en-note-de-prix")
    public NoteDePrixResponse convertirEnNote(@PathVariable Long id) { return NoteDePrixResponse.from(service.convertirEnNoteDePrix(id)); }
}
