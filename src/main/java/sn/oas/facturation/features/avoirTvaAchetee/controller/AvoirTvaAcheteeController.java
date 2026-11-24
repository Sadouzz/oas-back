package sn.oas.facturation.features.avoirTvaAchetee.controller;

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
import sn.oas.facturation.features.avoirTvaAchetee.dto.AvoirTvaAcheteeRequest;
import sn.oas.facturation.features.avoirTvaAchetee.dto.AvoirTvaAcheteeResponse;
import sn.oas.facturation.features.avoirTvaAchetee.service.AvoirTvaAcheteeService;
import sn.oas.facturation.shared.exception.BadRequestException;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/avoirs-tva-achetee")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER')")
@Tag(name = "Avoirs TVA achetée", description = "Gestion des avoirs sur TVA achetée pour la déclaration mensuelle")
public class AvoirTvaAcheteeController {

    private final AvoirTvaAcheteeService service;

    @GetMapping
    @Operation(summary = "Lister les avoirs TVA achetée d'un mois (YYYY-MM)")
    public ResponseEntity<Page<AvoirTvaAcheteeResponse>> listByMonth(
            @Parameter(description = "Mois au format YYYY-MM (défaut : mois courant)")
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        YearMonth ym = parseYearMonth(month);
        return ResponseEntity.ok(service.getByMonth(ym, page, size).map(AvoirTvaAcheteeResponse::from));
    }

    @PostMapping
    @Operation(summary = "Ajouter un avoir TVA achetée")
    public ResponseEntity<AvoirTvaAcheteeResponse> create(@Valid @RequestBody AvoirTvaAcheteeRequest request) {
        return new ResponseEntity<>(AvoirTvaAcheteeResponse.from(service.create(request)), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un avoir TVA achetée")
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
