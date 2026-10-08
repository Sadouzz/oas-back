package sn.oas.facturation.features.ordreReparation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RestitutionRequest(
        @NotBlank String signature,
        @Min(1) @Max(120) Integer garantieMois) {
}
