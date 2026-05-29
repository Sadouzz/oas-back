package sn.oas.facturation.bonDeSortie.dto;

public record LigneMainDoeuvreRequest(
        Long mainDoeuvreId,
        Integer quantite
) {}
