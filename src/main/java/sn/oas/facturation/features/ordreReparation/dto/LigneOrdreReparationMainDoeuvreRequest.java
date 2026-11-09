package sn.oas.facturation.features.ordreReparation.dto;

public record LigneOrdreReparationMainDoeuvreRequest(
        Long mainDoeuvreId,
        Integer nbreHeure,
        Integer prix,
        Double tauxRemise,
        Long id
) {
    public LigneOrdreReparationMainDoeuvreRequest(Long mainDoeuvreId, Integer nbreHeure, Integer prix) {
        this(mainDoeuvreId, nbreHeure, prix, null, null);
    }

    public LigneOrdreReparationMainDoeuvreRequest(Long mainDoeuvreId, Integer nbreHeure, Integer prix, Double tauxRemise) {
        this(mainDoeuvreId, nbreHeure, prix, tauxRemise, null);
    }
}
