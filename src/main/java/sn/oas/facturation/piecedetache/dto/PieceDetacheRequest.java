package sn.oas.facturation.piecedetache.dto;

import sn.oas.facturation.piecedetache.data.enums.StatutPiece;
import sn.oas.facturation.piecedetache.data.enums.TypePiece;

public record PieceDetacheRequest(
        TypePiece type,
        String reference,
        String designation,
        String categorie,
        Integer stockMagasin,
        Double prix,
        Double seuilMinimum
) {}