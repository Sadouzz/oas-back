package sn.oas.facturation.piecedetache.dto;

import java.util.List;

public record BonDeSortieRequest(
        Long clientId,
        Long vehiculeId,
        List<LigneBonDeSortieRequest> lignes,
        String remarque
) {}