package sn.oas.facturation.features.fournisseur.dto;

import lombok.Builder;
import sn.oas.facturation.features.fournisseur.data.entity.Fournisseur;

import java.time.LocalDateTime;

@Builder
public record FournisseurListResponse(
        Long id,
        String matricule,
        String nomEntreprise,
        String nom,
        String prenom,
        boolean archived
        // LocalDateTime createdAt
) {
    public static FournisseurListResponse from(Fournisseur f) {
        if (f == null)
            return null;
        return FournisseurListResponse.builder()
                .id(f.getId())
                .matricule(f.getMatricule())
                .nomEntreprise(f.getNomEntreprise())
                .nom(f.getNom())
                .prenom(f.getPrenom())
                .archived(f.isArchived())
                // .createdAt(f.getCreatedAt())
                .build();
    }
}
