package sn.oas.facturation.features.client.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AjoutCreditRequest(
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 13, fraction = 2) BigDecimal montant,
        @Size(max = 500) String commentaire) { }
