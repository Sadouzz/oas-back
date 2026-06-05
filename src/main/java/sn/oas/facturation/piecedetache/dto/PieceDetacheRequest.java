package sn.oas.facturation.piecedetache.dto;

import sn.oas.facturation.piecedetache.data.enums.StatutPiece;
import sn.oas.facturation.piecedetache.data.enums.TypePiece;

public record PieceDetacheRequest(
        TypePiece type,
        String numeroDeSerie,
        String reference,
        String categorie,
        Double pourcentage,
        StatutPiece statut,
        Integer stockMagasin,
        Double prix,
        Integer seuilMinimum
) {}