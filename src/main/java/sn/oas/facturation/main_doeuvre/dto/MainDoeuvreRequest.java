package sn.oas.facturation.main_doeuvre.dto;



public record MainDoeuvreRequest(
        Double prix,
        Long categorieId,
        Integer nbreHeure,
        Boolean isArchived) {
}
