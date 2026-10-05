package sn.oas.facturation.features.pdfTemplate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.pdfTemplate.data.dto.PdfTemplateResponse;
import sn.oas.facturation.features.pdfTemplate.data.dto.PdfTemplateWriteRequest;
import sn.oas.facturation.features.pdfTemplate.service.PdfTemplateService;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/pdf-templates")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PdfTemplateController {
    private final PdfTemplateService service;
    private final UserRepository userRepository;
    private final sn.oas.facturation.features.pdfGenerator.service.HtmlToPdfService htmlToPdfService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_AGENT', 'MASTER')")
    public List<PdfTemplateResponse> list(@RequestParam(required = false) String documentType) { return service.list(documentType); }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_AGENT', 'MASTER')")
    public PdfTemplateResponse create(Authentication auth, @Valid @RequestBody PdfTemplateWriteRequest request) {
        return service.create(request, currentUser(auth));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_AGENT', 'MASTER')")
    public PdfTemplateResponse revise(@PathVariable Long id, Authentication auth, @Valid @RequestBody PdfTemplateWriteRequest request) {
        return service.revise(id, request, currentUser(auth));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('SUPER_AGENT', 'MASTER')")
    public PdfTemplateResponse activate(@PathVariable Long id) { return service.activate(id); }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_AGENT', 'MASTER')")
    public PdfTemplateResponse deactivate(@PathVariable Long id) { return service.deactivate(id); }

    @PostMapping(value = "/preview", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('SUPER_AGENT', 'MASTER')")
    public ResponseEntity<byte[]> preview(@RequestBody PdfTemplateWriteRequest request) {
        String html = service.renderPreview(request.documentType(), request.blocks(), Map.ofEntries(
                Map.entry("numero", "FC-2026-0001"), Map.entry("date", "04/10/2026"), Map.entry("agentNom", "FAYE"), Map.entry("clientNom", "Client Exemple"),
                Map.entry("immatriculation", "DK-1234-AA"), Map.entry("marque", "Mercedes-Benz"), Map.entry("modele", "Classe C"),
                Map.entry("annee", "2018"), Map.entry("chassis", "WDD00000000000000"), Map.entry("numeroBonDeCommande", "BC-2026-1001"),
                Map.entry("kilometrage", "95 000 km"), Map.entry("montantHT", "118 064 F CFA"), Map.entry("montantTVA", "21 251 F CFA"),
                Map.entry("montantTimbre", "0 F CFA"), Map.entry("montantAutre", "0 F CFA"), Map.entry("montantTotal", "139 315 F CFA"),
                Map.entry("montantTTC", "139 315 F CFA"), Map.entry("montantPaye", "0 F CFA"), Map.entry("resteAPayer", "139 315 F CFA"), Map.entry("remarque", "Exemple d'aperçu")), Map.of(
                "LIGNES_PIECES", List.of(Map.of("reference", "IG012025", "designation", "Huile moteur 10W40", "quantite", "1", "prixUnitaire", "4 133", "montant", "4 133")),
                "LIGNES_MAIN_DOEUVRE", List.of(Map.of("designation", "Vidange entretien moteur", "heures", "1", "quantite", "1", "prixUnitaire", "27 500", "montant", "27 500"))));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(htmlToPdfService.genererApercuPdf(html));
    }

    @GetMapping("/active-invoice")
    public List<PdfTemplateResponse> activeInvoiceTemplates(@RequestParam String layoutKey) {
        return service.activeInvoiceTemplates(layoutKey);
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName()).orElseGet(() -> userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Utilisateur introuvable.")));
    }
}
