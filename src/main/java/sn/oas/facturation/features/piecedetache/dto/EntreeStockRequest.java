package sn.oas.facturation.features.piecedetache.dto;

public record EntreeStockRequest(
        Long pieceId,
        Integer quantite,
        String motif
) {}
