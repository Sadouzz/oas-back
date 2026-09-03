package sn.oas.facturation.features.marketplace.dto;

import lombok.Builder;
import sn.oas.facturation.features.marketplace.data.entity.Produit;

@Builder
public record ProduitListResponse(
        Long id,
        String nom,
        String description,
        Double prix,
        String mediaUrl,
        Boolean disponible,
        Boolean archive
) {
    public static ProduitListResponse from(Produit p) {
        if (p == null) return null;
        return ProduitListResponse.builder()
                .id(p.getId())
                .nom(p.getNom())
                .description(p.getDescription())
                .prix(p.getPrix())
                .mediaUrl(p.getMediaUrl())
                .disponible(p.getDisponible())
                .archive(p.getArchive())
                .build();
    }
}
