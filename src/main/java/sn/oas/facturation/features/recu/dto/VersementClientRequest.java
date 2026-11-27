package sn.oas.facturation.features.recu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VersementClientRequest(
        @NotNull(message = "Le montant du versement est obligatoire")
        @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à zéro")
        BigDecimal montant,
        String modePaiement,
        String remarque
) {
}
