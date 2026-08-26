package sn.oas.facturation.piecedetache.dto;

import sn.oas.facturation.piecedetache.data.enums.TypeAlerte;

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
