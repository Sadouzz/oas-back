package sn.oas.facturation.features.main_doeuvre.dto;


public record MainDoeuvreRequest(
        Double prix,
        String description,
        Long categorieId,
        Integer nbreHeure,
        Boolean isArchived) {
}
