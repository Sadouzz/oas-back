package sn.oas.facturation.features.categorie_pieces.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CategorieRequest(
        @NotBlank(message = "Le nom de la catégorie est obligatoire")
        String nom,
        Long depotId,
        DepotSummary depot
) {
    public record DepotSummary(Long id, String nom) {}

    public Long getEffectiveDepotId() {
        if (depotId != null) return depotId;
        if (depot != null && depot.id() != null) return depot.id();
        return null;
    }
}
