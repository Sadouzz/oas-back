package sn.oas.facturation.features.client.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/** Conditions de crédit communes aux particuliers et aux entreprises. Les null suppriment la limite concernée. */
public record ConditionsFinancieresRequest(
        @PositiveOrZero Integer plafondEncours,
        @Min(1) @Max(3650) Integer echeanceJours,
        @DecimalMin("0.0") BigDecimal plafondPeriode) { }
