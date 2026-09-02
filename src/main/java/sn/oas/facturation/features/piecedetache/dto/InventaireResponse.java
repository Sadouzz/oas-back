package sn.oas.facturation.features.piecedetache.dto;

import sn.oas.facturation.features.piecedetache.data.entity.StockMouvement;

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
        StockMouvement mouvement
) {}
