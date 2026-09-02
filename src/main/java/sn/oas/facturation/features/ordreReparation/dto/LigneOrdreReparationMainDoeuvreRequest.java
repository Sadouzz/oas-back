package sn.oas.facturation.features.ordreReparation.dto;

public record LigneOrdreReparationMainDoeuvreRequest(
        Long mainDoeuvreId,
        Integer nbreHeure,
        Integer prix
) {}
