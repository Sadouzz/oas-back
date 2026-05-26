package sn.oas.facturation.piecedetache.dto;

public record EntreeStockRequest(
        Long pieceId,
        Integer quantite,
        String motif
) {}
