package sn.oas.facturation.features.depot_pieces.dto.response;

import sn.oas.facturation.features.depot_pieces.data.entity.Depot;

public record DepotResponse(
        Long id,
        String nom,
        String description
) {
    public static DepotResponse from(Depot depot) {
        if (depot == null) return null;
        return new DepotResponse(
                depot.getId(),
                depot.getNom(),
                depot.getDescription()
        );
    }
}
