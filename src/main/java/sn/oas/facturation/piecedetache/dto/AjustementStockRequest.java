package sn.oas.facturation.piecedetache.dto;

public record AjustementStockRequest(
        Long pieceId,
        Integer stockMagasin,
        Integer stockAtelier,
        String motif
) {}
