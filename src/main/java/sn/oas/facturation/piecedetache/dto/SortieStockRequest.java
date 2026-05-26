package sn.oas.facturation.piecedetache.dto;

public record SortieStockRequest(
        Long pieceId,
        Integer quantite,
        String motif
) {}
