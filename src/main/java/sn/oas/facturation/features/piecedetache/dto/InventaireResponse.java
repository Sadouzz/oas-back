package sn.oas.facturation.features.piecedetache.dto;

import sn.oas.facturation.features.piecedetache.data.entity.PieceMouvement;

public record InventaireResponse(
        Long pieceId,
        String numeroDeSerie,
        String reference,
        Double stockMagasinTheorique,
        Double stockAtelierTheorique,
        Double stockMagasinPhysique,
        Double stockAtelierPhysique,
        Double ecartMagasin,
        Double ecartAtelier,
        boolean ajuste,
        PieceMouvement mouvement
) {}
