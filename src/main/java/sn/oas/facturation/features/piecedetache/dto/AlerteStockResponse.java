package sn.oas.facturation.features.piecedetache.dto;

import sn.oas.facturation.features.piecedetache.data.enums.TypeAlerte;

public record AlerteStockResponse(
        Long pieceId,
        String numeroDeSerie,
        String reference,
        String categorie,
        Double stockMagasin,
        Double stockAtelier,
        Double qteReelle,
        Double seuilApplique,
        TypeAlerte typeAlerte
) {}
