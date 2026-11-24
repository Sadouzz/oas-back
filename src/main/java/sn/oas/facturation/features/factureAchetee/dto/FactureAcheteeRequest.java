package sn.oas.facturation.features.factureAchetee.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FactureAcheteeRequest(
        @NotBlank(message = "Le numéro de facture est obligatoire")
        String numeroFacture,
        @NotNull(message = "La date de la facture est obligatoire")
        LocalDate dateFacture,
        @NotNull(message = "Le montant de la TVA achetée est obligatoire")
        @DecimalMin(value = "0.00", message = "Le montant de la TVA achetée doit être positif ou nul")
        BigDecimal montantTva
) {
}
