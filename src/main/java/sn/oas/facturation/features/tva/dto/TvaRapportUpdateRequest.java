package sn.oas.facturation.features.tva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TvaRapportUpdateRequest(
        @NotBlank(message = "Le mois (YYYY-MM) est obligatoire")
        String month,
        @NotNull(message = "Le montant du report est obligatoire")
        BigDecimal montant,
        Boolean verrouilleManuellement
) {
}
