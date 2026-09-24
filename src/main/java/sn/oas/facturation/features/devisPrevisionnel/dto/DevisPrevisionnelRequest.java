package sn.oas.facturation.features.devisPrevisionnel.dto;

import java.math.BigDecimal;

public record DevisPrevisionnelRequest(
        String notesReparation,
        BigDecimal montantTotal,
        Double kilometrageVehicule,
        Long vehiculeId,
        Long clientId,
        Long ficheAtelierId,
        Long ordreReparationId
) {
    public DevisPrevisionnelRequest(String notesReparation, BigDecimal montantTotal, Double kilometrageVehicule, Long vehiculeId, Long clientId, Long ficheAtelierId) {
        this(notesReparation, montantTotal, kilometrageVehicule, vehiculeId, clientId, ficheAtelierId, null);
    }
}
