package sn.oas.facturation.piecedetache.dto;

import sn.oas.facturation.piecedetache.data.enums.TypeAlerte;

public record AlerteStockResponse(
        Long pieceId,
        String numeroDeSerie,
        String reference,
        String categorie,
        Integer stockMagasin,
        Integer stockAtelier,
        Integer qteReelle,
        Integer seuilApplique,
        TypeAlerte typeAlerte
) {}
