package sn.oas.facturation.piecedetache.dto;

public record InventaireRequest(
        Long pieceId,
        Integer stockMagasinPhysique,
        Integer stockAtelierPhysique,
        String motif
) {}
