package sn.oas.facturation.features.avoirTvaAchetee.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AvoirTvaAcheteeRequest(
        @NotBlank(message = "La désignation est obligatoire")
        String designation,
        @NotNull(message = "La date est obligatoire")
        LocalDate dateOperation,
        @NotNull(message = "Le montant de l'avoir TVA achetée est obligatoire")
        @DecimalMin(value = "0.00", message = "Le montant de l'avoir TVA achetée doit être positif ou nul")
        BigDecimal montantTva
) {
}
