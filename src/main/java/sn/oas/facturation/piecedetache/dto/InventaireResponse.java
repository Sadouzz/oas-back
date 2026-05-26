package sn.oas.facturation.piecedetache.dto;

import sn.oas.facturation.piecedetache.data.entity.StockMouvement;

public record InventaireResponse(
        Long pieceId,
        String numeroDeSerie,
        String reference,
        Integer stockMagasinTheorique,
        Integer stockAtelierTheorique,
        Integer stockMagasinPhysique,
        Integer stockAtelierPhysique,
        Integer ecartMagasin,
        Integer ecartAtelier,
        boolean ajuste,
        StockMouvement mouvement
) {}
