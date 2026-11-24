package sn.oas.facturation.features.tva.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.oas.facturation.features.tva.data.entity.RapportTvaMensuel;
import sn.oas.facturation.features.tva.dto.TvaDeclarationResponseDto;
import sn.oas.facturation.features.tva.dto.TvaRapportUpdateRequest;
import sn.oas.facturation.features.tva.service.TvaDeclarationService;
import sn.oas.facturation.shared.exception.BadRequestException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/tva")
@RequiredArgsConstructor
@Tag(name = "Gestion TVA", description = "Déclaration mensuelle de TVA (factures vendues/achetées, avoirs, report et PDF)")
public class TvaDeclarationController {

    private static final DateTimeFormatter FILE_DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yy");

    private final TvaDeclarationService tvaDeclarationService;

    @GetMapping("/declaration")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    @Operation(
            summary = "Récupérer la déclaration mensuelle de TVA",
            description = "Agrège pour un mois donné (YYYY-MM) les factures vendues, avoirs sur ventes, factures achetées, avoirs TVA achetée, totaux, report du mois précédent et solde.")
    public ResponseEntity<TvaDeclarationResponseDto> getDeclaration(
            @Parameter(description = "Mois au format YYYY-MM (défaut : mois courant)")
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "0") int pageVendu,
            @RequestParam(defaultValue = "0") int pageAvoirVendu,
            @RequestParam(defaultValue = "0") int pageAchete,
            @RequestParam(defaultValue = "0") int pageAvoirAchete,
            @RequestParam(defaultValue = "10") int size) {
        YearMonth ym = parseYearMonth(month);
        return ResponseEntity.ok(tvaDeclarationService.getDeclaration(
                ym, pageVendu, pageAvoirVendu, pageAchete, pageAvoirAchete, size));
    }

    @GetMapping(value = "/declaration/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
    @Operation(summary = "Exporter / imprimer la liste mensuelle TVA en PDF")
    public ResponseEntity<byte[]> downloadPdf(
            @Parameter(description = "Mois au format YYYY-MM (défaut : mois courant)")
            @RequestParam(required = false) String month) {
        YearMonth ym = parseYearMonth(month);
        byte[] pdfBytes = tvaDeclarationService.generateDeclarationPdf(ym);
        String filename = "Liste-des-factures-" + LocalDate.now().format(FILE_DATE_FMT) + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @PutMapping("/rapport")
    @PreAuthorize("hasAnyRole('SUPER_AGENT', 'MASTER')")
    @Operation(summary = "Définir ou corriger manuellement le report de crédit de TVA d'un mois (<= 0)")
    public ResponseEntity<RapportTvaMensuel> updateRapport(@Valid @RequestBody TvaRapportUpdateRequest request) {
        return ResponseEntity.ok(tvaDeclarationService.updateRapport(request));
    }

    private YearMonth parseYearMonth(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month.trim());
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Format de mois invalide (attendu YYYY-MM) : " + month);
        }
    }
}
