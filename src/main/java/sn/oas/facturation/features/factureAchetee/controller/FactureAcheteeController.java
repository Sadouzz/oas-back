package sn.oas.facturation.features.factureAchetee.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.oas.facturation.features.factureAchetee.dto.FactureAcheteeRequest;
import sn.oas.facturation.features.factureAchetee.dto.FactureAcheteeResponse;
import sn.oas.facturation.features.factureAchetee.service.FactureAcheteeService;
import sn.oas.facturation.shared.exception.BadRequestException;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/factures-achetees")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
@Tag(name = "Factures achetées (TVA)", description = "Gestion des factures d'achat pour la déclaration mensuelle de TVA")
public class FactureAcheteeController {

    private final FactureAcheteeService service;

    @GetMapping
    @Operation(summary = "Lister les factures achetées d'un mois (YYYY-MM)")
    public ResponseEntity<Page<FactureAcheteeResponse>> listByMonth(
            @Parameter(description = "Mois au format YYYY-MM (défaut : mois courant)")
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        YearMonth ym = parseYearMonth(month);
        return ResponseEntity.ok(service.getByMonth(ym, page, size).map(FactureAcheteeResponse::from));
    }

    @PostMapping
    @Operation(summary = "Ajouter une facture achetée")
    public ResponseEntity<FactureAcheteeResponse> create(@Valid @RequestBody FactureAcheteeRequest request) {
        return new ResponseEntity<>(FactureAcheteeResponse.from(service.create(request)), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une facture achetée")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
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
