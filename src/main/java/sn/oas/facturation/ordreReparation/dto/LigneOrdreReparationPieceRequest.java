package sn.oas.facturation.ordreReparation.dto;

public record LigneOrdreReparationPieceRequest(
        Long pieceId,
        Integer quantite,
        Integer prix
) {}
