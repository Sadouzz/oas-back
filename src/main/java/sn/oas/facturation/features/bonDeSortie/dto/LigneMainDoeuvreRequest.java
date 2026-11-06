package sn.oas.facturation.features.bonDeSortie.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LigneMainDoeuvreRequest(
        Long mainDoeuvreId,
        @JsonAlias({"heures", "nbreHeures", "heuresFacturees"})
        Integer nbreHeure,
        Integer quantite,
        @JsonAlias({"tauxHoraire"})
        Double tarifHoraire,
        @JsonAlias({"price"})
        Double prix,
        Double tauxRemise,
        String designation,
        Long proformaLineId
) {
    public LigneMainDoeuvreRequest(Long mainDoeuvreId, Integer nbreHeure, Double tarifHoraire, String designation) {
        this(mainDoeuvreId, nbreHeure, null, tarifHoraire, null, null, designation, null);
    }

    @JsonIgnore
    public Integer getEffectiveHeures() {
        if (nbreHeure != null && nbreHeure > 0) return nbreHeure;
        if (quantite != null && quantite > 0) return quantite;
        return 1;
    }

    @JsonIgnore
    public Double getEffectiveTarif() {
        if (tarifHoraire != null) return tarifHoraire;
        if (prix != null) return prix;
        return null;
    }
}
