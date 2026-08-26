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
        Double stockMagasin,
        Double prix,
        Double seuilMinimum
) {}