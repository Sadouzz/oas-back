package sn.oas.facturation.features.bonDeSortie.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record LignePieceRequest(
        Long pieceId,
        Integer quantite,
        @JsonAlias({"prixUnitaire", "prix_unitaire", "price"})
        Double prix,
        Double prixUnitaire,
        String type,
        Long mainDoeuvreId,
        String designation,
        @JsonAlias({"remise", "taux_remise", "discount", "tauxRemise"})
        Double tauxRemise
) {
    public LignePieceRequest(Long pieceId, Integer quantite) {
        this(pieceId, quantite, null, null, null, null, null, null);
    }

    public LignePieceRequest(Long pieceId, Integer quantite, Double prix) {
        this(pieceId, quantite, prix, prix, null, null, null, null);
    }

    public LignePieceRequest(Long pieceId, Integer quantite, Double prix, Double prixUnitaire, String type, Long mainDoeuvreId, String designation) {
        this(pieceId, quantite, prix, prixUnitaire, type, mainDoeuvreId, designation, null);
    }

    public Double getEffectivePrix() {
        if (prix != null) return prix;
        if (prixUnitaire != null) return prixUnitaire;
        return null;
    }

    public Double getEffectiveTauxRemise() {
        return tauxRemise != null && tauxRemise >= 0 ? tauxRemise : 0.0;
    }
}
