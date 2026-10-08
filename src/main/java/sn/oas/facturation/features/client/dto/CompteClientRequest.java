package sn.oas.facturation.features.client.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CompteClientRequest(
        @NotBlank @Size(max = 200) String raisonSociale,
        @NotBlank @Pattern(regexp = "(?i)[A-Z0-9]{3,40}", message = "Le NINEA doit contenir entre 3 et 40 lettres ou chiffres.") String ninea,
        @NotNull @Min(0) @Max(100) Integer remisePourcentage,
        @DecimalMin("0.0") @jakarta.validation.constraints.DecimalMax("2147483647") BigDecimal plafondCredit,
        @Min(1) @Max(3650) Integer echeanceJours,
        @DecimalMin("0.0") BigDecimal plafondPeriode,
        @Size(max = 100) String rccm,
        @Size(max = 100) String rib,
        Boolean actif) { }
