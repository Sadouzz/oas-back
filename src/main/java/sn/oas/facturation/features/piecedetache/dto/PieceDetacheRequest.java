package sn.oas.facturation.features.piecedetache.dto;

import sn.oas.facturation.features.piecedetache.data.enums.TypePiece;

public record PieceDetacheRequest(
        TypePiece type,
        String reference,
        String designation,
        String categorie,
        Integer stockMagasin,
        Double prix,
        Double seuilMinimum
) {}