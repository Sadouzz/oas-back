package sn.oas.facturation.features.piecedetache.dto;

public record AjustementStockRequest(
        Long pieceId,
        Integer stockMagasin,
        Integer stockAtelier,
        String motif
) {}
