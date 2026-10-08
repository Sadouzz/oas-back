package sn.oas.facturation.features.categorie_pieces.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CategorieRequest(
        @NotBlank(message = "Le nom de la catégorie est obligatoire")
        String nom,
        Long depotId,
        //DepotSummary depot,
        List<Long> depotIds,
        List<DepotSummary> depots
) {
    public record DepotSummary(Long id, String nom) {}

    public List<Long> getEffectiveDepotIds() {
        List<Long> ids = new ArrayList<>();
        if (depotIds != null) {
            ids.addAll(depotIds.stream().filter(java.util.Objects::nonNull).toList());
        }
        if (depots != null) {
            depots.stream().filter(d -> d != null && d.id() != null).map(DepotSummary::id).forEach(ids::add);
        }
        if (depotId != null && !ids.contains(depotId)) {
            ids.add(depotId);
        }
        /*
        if (depot != null && depot.id() != null && !ids.contains(depot.id())) {
            ids.add(depot.id());
        }
            */
        return ids.stream().distinct().toList();
    }
}
