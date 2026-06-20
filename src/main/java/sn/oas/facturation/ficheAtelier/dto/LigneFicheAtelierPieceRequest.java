package sn.oas.facturation.ficheAtelier.dto;

public record LigneFicheAtelierPieceRequest(
        Long pieceId,
        Integer quantite,
        Integer prix
) {}
