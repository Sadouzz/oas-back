package sn.oas.facturation.features.bonDeLivraison.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonCreateRequest;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonFactureRequest;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonOrdreContextResponse;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonResponse;
import sn.oas.facturation.features.bonDeLivraison.dto.FactureDepuisOrdreResponse;
import sn.oas.facturation.features.bonDeSortie.dto.BonDeSortieDisponibleResponse;
import sn.oas.facturation.features.bonDeLivraison.service.BonDeLivraisonService;
import sn.oas.facturation.features.facture.dto.FactureResponse;
import sn.oas.facturation.features.noteDePrix.dto.NoteDePrixResponse;
import java.util.List;

@RestController
@RequestMapping("/api/bons-de-livraison")
@RequiredArgsConstructor
@Tag(name = "Bons de livraison", description = "Livraisons de véhicules depuis les bons de sortie d'un ordre de réparation")
@PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
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
    public List<BonDeSortieDisponibleResponse> bonsDeSortie(@PathVariable Long ordreId) { return service.bonsDeSortieDisponibles(ordreId); }
    @GetMapping("/ordres-reparation/{ordreId}/contexte")
    public BonDeLivraisonOrdreContextResponse contexteOrdre(@PathVariable Long ordreId) { return service.contexteOrdre(ordreId); }
    @PostMapping("/ordres-reparation/{ordreId}/facture")
    public FactureDepuisOrdreResponse creerFactureDepuisOrdre(@PathVariable Long ordreId,
            @RequestBody(required = false) BonDeLivraisonFactureRequest request) {
        var facture = service.creerFactureDepuisOrdre(ordreId, request);
        return new FactureDepuisOrdreResponse(facture.getId(), facture.getNumero());
    }
    @PostMapping("/{id}/convertir-en-facture")
    public FactureResponse convertirEnFacture(@PathVariable Long id) { return FactureResponse.from(service.convertirEnFacture(id)); }
    @PostMapping("/{id}/convertir-en-note-de-prix")
    public NoteDePrixResponse convertirEnNote(@PathVariable Long id) { return NoteDePrixResponse.from(service.convertirEnNoteDePrix(id)); }
}
