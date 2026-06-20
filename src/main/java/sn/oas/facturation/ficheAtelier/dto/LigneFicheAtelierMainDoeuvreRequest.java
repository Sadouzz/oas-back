package sn.oas.facturation.ficheAtelier.dto;

public record LigneFicheAtelierMainDoeuvreRequest(
        Long mainDoeuvreId,
        Integer nbreHeure,
        Integer prix
) {}
