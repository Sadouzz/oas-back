package sn.oas.facturation.features.categorie_pieces.dto.response;

import sn.oas.facturation.features.categorie_pieces.data.entity.Categorie;
import sn.oas.facturation.features.depot_pieces.dto.response.DepotResponse;

public record CategorieResponse(
        Long id,
        String nom,
        DepotResponse depot
) {
    public static CategorieResponse from(Categorie categorie) {
        if (categorie == null) return null;
        return new CategorieResponse(
                categorie.getId(),
                categorie.getNom(),
                categorie.getDepot() != null ? DepotResponse.from(categorie.getDepot()) : null
        );
    }
}
