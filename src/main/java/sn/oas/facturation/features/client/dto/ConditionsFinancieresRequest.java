package sn.oas.facturation.features.client.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

/** Conditions de paiement communes aux particuliers et aux entreprises. */
public record ConditionsFinancieresRequest(
        @Min(1) @Max(3650) Integer echeanceJours,
        @DecimalMin("0.0") BigDecimal plafondPeriode) { }
