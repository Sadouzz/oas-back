package sn.oas.facturation.features.depot_pieces.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DepotRequest(
        @NotBlank(message = "Le nom du dépôt est obligatoire")
        String nom,
        String description
) {}
