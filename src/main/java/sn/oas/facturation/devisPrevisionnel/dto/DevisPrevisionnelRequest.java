package sn.oas.facturation.devisPrevisionnel.dto;

import java.math.BigDecimal;

public record DevisPrevisionnelRequest(
        String notesReparation,
        BigDecimal montantTotal,
        Double kilometrageVehicule,
        Long vehiculeId,
        Long clientId
) {}
