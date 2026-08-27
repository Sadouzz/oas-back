package sn.oas.facturation.piecedetache.dto;

public record InventaireRequest(
        Long pieceId,
        Double stockMagasinPhysique,
        Double stockAtelierPhysique,
        String motif
) {}
