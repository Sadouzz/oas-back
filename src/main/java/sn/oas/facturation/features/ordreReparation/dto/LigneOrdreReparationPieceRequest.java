package sn.oas.facturation.features.ordreReparation.dto;

public record LigneOrdreReparationPieceRequest(
        Long pieceId,
        Integer quantite,
        Integer prix,
        Boolean isCustom,
        String designationPds
) {}
