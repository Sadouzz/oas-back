package sn.oas.facturation.recu.dto;

import sn.oas.facturation.recu.data.entity.Recu;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecuResponse(
        Long id,
        String numero,
        Long factureId,
        String factureNumero,
        BigDecimal montantPaye,
        LocalDateTime datePaiement,
        String methodePaiement,
        String clientName,
        String vehiculeImmatriculation
) {
    public static RecuResponse of(Recu recu) {
        return new RecuResponse(
                recu.getId(),
                recu.getNumero(),
                recu.getFacture().getId(),
                recu.getFacture().getNumero(),
                recu.getMontantPaye(),
                recu.getDatePaiement(),
                recu.getMethodePaiement(),
                recu.getFacture().getClient().getFirstName() + " " + recu.getFacture().getClient().getLastName(),
                recu.getFacture().getVehicule() != null ? recu.getFacture().getVehicule().getImmatriculation() : null
        );
    }
}
