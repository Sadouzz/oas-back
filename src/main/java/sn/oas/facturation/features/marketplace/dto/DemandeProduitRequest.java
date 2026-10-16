package sn.oas.facturation.features.marketplace.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DemandeProduitRequest(
        @NotNull Long produitId,
        @Positive Integer quantite,
        @Size(max = 4000) String message) {
}
