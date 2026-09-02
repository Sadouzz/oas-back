package sn.oas.facturation.features.piecedetache.dto;

public record InventaireRequest(
        Long pieceId,
        Double stockMagasinPhysique,
        Double stockAtelierPhysique,
        String motif
) {}
