package sn.oas.facturation.main_doeuvre.dto;

import sn.oas.facturation.main_doeuvre.data.entity.CategorieMainDoeuvre;

public record MainDoeuvreRequest(
        Double prix,
        CategorieMainDoeuvre categorie,
        Integer nbreHeure,
        Boolean isArchived) {
}
