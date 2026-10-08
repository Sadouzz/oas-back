package sn.oas.facturation.features.categorie_pieces.dto.response;

import java.util.List;
import sn.oas.facturation.features.categorie_pieces.data.entity.Categorie;
import sn.oas.facturation.features.depot_pieces.dto.response.DepotResponse;

public record CategorieResponse(
        Long id,
        String nom,
        List<DepotResponse> depots
) {
    public static CategorieResponse from(Categorie categorie) {
        if (categorie == null) return null;
        List<DepotResponse> depotResponses = categorie.getDepots() != null
                ? categorie.getDepots().stream().map(DepotResponse::from).toList()
                : java.util.Collections.emptyList();
        return new CategorieResponse(
                categorie.getId(),
                categorie.getNom(),
                depotResponses
        );
    }
}
