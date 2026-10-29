package sn.oas.facturation.features.fournisseur.dto;

import lombok.Builder;
import sn.oas.facturation.features.fournisseur.data.entity.Fournisseur;


@Builder
public record FournisseurListResponse(
        Long id,
        String numero,
        String nom,
        boolean archived
        // LocalDateTime createdAt
) {
    public static FournisseurListResponse from(Fournisseur f) {
        if (f == null)
            return null;
        return FournisseurListResponse.builder()
                .id(f.getId())
                .numero(f.getNumero())
                .nom(f.getNom())
                .archived(f.isArchived())
                // .createdAt(f.getCreatedAt())
                .build();
    }
}
