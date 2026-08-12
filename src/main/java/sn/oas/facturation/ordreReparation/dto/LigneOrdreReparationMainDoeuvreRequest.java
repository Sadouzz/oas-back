package sn.oas.facturation.ordreReparation.dto;

public record LigneOrdreReparationMainDoeuvreRequest(
        Long mainDoeuvreId,
        Integer nbreHeure,
        Integer prix
) {}
