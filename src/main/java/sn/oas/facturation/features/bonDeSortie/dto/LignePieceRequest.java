package sn.oas.facturation.features.bonDeSortie.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LignePieceRequest(
        Long pieceId,
        Integer quantite,
        @JsonAlias({"price"})
        Double prix,
        Double prixUnitaire,
        Double tauxRemise,
        String type,
        Long mainDoeuvreId,
        String designation,
        Long proformaLineId
) {
    public LignePieceRequest(Long pieceId, Integer quantite) {
        this(pieceId, quantite, null, null, null, null, null, null, null);
    }

    public LignePieceRequest(Long pieceId, Integer quantite, Double prix) {
        this(pieceId, quantite, prix, prix, null, null, null, null, null);
    }

    public LignePieceRequest(Long pieceId, Integer quantite, Double prix, Double prixUnitaire, String type, Long mainDoeuvreId, String designation) {
        this(pieceId, quantite, prix, prixUnitaire, null, type, mainDoeuvreId, designation, null);
    }

    public LignePieceRequest(Long pieceId, Integer quantite, Double prix, Double prixUnitaire, String type, Long mainDoeuvreId, String designation, Long proformaLineId) {
        this(pieceId, quantite, prix, prixUnitaire, null, type, mainDoeuvreId, designation, proformaLineId);
    }

    @JsonIgnore
    public Double getEffectivePrix() {
        if (prixUnitaire != null) return prixUnitaire;
        if (prix != null) return prix;
        return null;
    }
}
