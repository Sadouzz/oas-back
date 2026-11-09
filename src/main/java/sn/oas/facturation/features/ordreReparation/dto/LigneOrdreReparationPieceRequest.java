package sn.oas.facturation.features.ordreReparation.dto;

public record LigneOrdreReparationPieceRequest(
        Long pieceId,
        Integer quantite,
        Integer prix,
        Boolean isCustom,
        String designationPds,
        Double tauxRemise,
        Long id
) {
    public LigneOrdreReparationPieceRequest(Long pieceId, Integer quantite, Integer prix, Boolean isCustom, String designationPds) {
        this(pieceId, quantite, prix, isCustom, designationPds, null, null);
    }

    public LigneOrdreReparationPieceRequest(Long pieceId, Integer quantite, Integer prix, Boolean isCustom, String designationPds, Double tauxRemise) {
        this(pieceId, quantite, prix, isCustom, designationPds, tauxRemise, null);
    }
}
