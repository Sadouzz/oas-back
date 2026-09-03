package sn.oas.facturation.features.main_doeuvre.dto;

import lombok.Builder;
import sn.oas.facturation.features.main_doeuvre.data.entity.MainDoeuvre;

@Builder
public record MainDoeuvreListResponse(
        Long id,
        Double prix,
        String description,
        CategorieSummary categorie,
        Integer nbreHeure,
        Boolean isArchived
) {
    public record CategorieSummary(Long id, String nom) {}

    public static MainDoeuvreListResponse from(MainDoeuvre m) {
        if (m == null) return null;

        CategorieSummary catSummary = null;
        if (m.getCategorie() != null) {
            catSummary = new CategorieSummary(m.getCategorie().getId(), m.getCategorie().getNom());
        }

        return MainDoeuvreListResponse.builder()
                .id(m.getId())
                .prix(m.getPrix())
                .description(m.getDescription())
                .categorie(catSummary)
                .nbreHeure(m.getNbreHeure())
                .isArchived(m.getIsArchived())
                .build();
    }
}
