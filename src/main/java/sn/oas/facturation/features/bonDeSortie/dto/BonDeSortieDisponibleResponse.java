package sn.oas.facturation.features.bonDeSortie.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BonDeSortieDisponibleResponse(
        Long id,
        String reference,
        LocalDateTime date,
        BigDecimal montant,
        Long clientId,
        Long vehiculeId,
        Long ordreReparationId) {
    public BonDeSortieDisponibleResponse {
        montant = montant == null ? BigDecimal.ZERO : montant;
    }
}
