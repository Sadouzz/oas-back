package sn.oas.facturation.features.ordreReparation.dto;

public record LigneOrdreReparationPieceRequest(
        Long pieceId,
        Integer quantite,
        Integer prix,
        Boolean isCustom,
        String designationPds,
        Double tauxRemise
) {
    public LigneOrdreReparationPieceRequest(Long pieceId, Integer quantite, Integer prix, Boolean isCustom, String designationPds) {
        this(pieceId, quantite, prix, isCustom, designationPds, null);
    }
}
