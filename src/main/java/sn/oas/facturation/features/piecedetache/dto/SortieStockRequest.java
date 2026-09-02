package sn.oas.facturation.features.piecedetache.dto;

public record SortieStockRequest(
        Long pieceId,
        Integer quantite,
        String motif
) {}
