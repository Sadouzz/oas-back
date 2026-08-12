package sn.oas.facturation.bonDeSortie.dto;

import java.util.List;

public record BonDeSortieRequest(
        Long clientId,
        Long vehiculeId,
        List<LignePieceRequest> lignesPieces,
        Long ordreReparationId,
        String remarque
) {}