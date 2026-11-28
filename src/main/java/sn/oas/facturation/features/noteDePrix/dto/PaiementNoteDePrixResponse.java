package sn.oas.facturation.features.noteDePrix.dto;

import sn.oas.facturation.features.noteDePrix.data.entity.PaiementNoteDePrix;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaiementNoteDePrixResponse(
        Long id,
        BigDecimal montant,
        String modePaiement,
        LocalDateTime datePaiement,
        Long acteurId) {

    public static PaiementNoteDePrixResponse from(PaiementNoteDePrix paiement) {
        return new PaiementNoteDePrixResponse(paiement.getId(), paiement.getMontant(), paiement.getModePaiement(),
                paiement.getDatePaiement(), paiement.getActeurId());
    }
}
