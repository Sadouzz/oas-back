package sn.oas.facturation.features.bonDeSortie.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record LigneMainDoeuvreRequest(
        Long mainDoeuvreId,
        @JsonAlias({"quantite", "qte", "heures", "nbreHeures", "heuresFacturees"})
        Integer nbreHeure,
        @JsonAlias({"prix", "price", "prixUnitaire", "tarif", "tauxHoraire"})
        Double tarifHoraire,
        Double tauxRemise,
        String designation
) {
    public Integer getEffectiveHeures() {
        return nbreHeure != null && nbreHeure > 0 ? nbreHeure : 1;
    }

    public Double getEffectiveTarif() {
        if (tarifHoraire != null) return tarifHoraire;
        return null;
    }
}
