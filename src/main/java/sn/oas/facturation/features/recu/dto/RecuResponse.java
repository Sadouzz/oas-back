package sn.oas.facturation.features.recu.dto;

import sn.oas.facturation.features.recu.data.entity.Recu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecuResponse {
    private Long id;
    private String numero;
    private Long factureId;
    private BigDecimal montant;
    private String modePaiement;
    // private String remarque;
    private LocalDateTime datePaiement;

    private String clientNom;
    private String numeroFacture;
    private String numeroOrdreReparation;

    public static RecuResponse from(Recu r) {
        if (r == null) return null;
        String clientNom = null;
        String numeroFacture = null;
        String numeroOrdreReparation = null;
        Long factureId = null;

        if (r.getFacture() != null) {
            factureId = r.getFacture().getId();
            numeroFacture = r.getFacture().getNumero();
            if (r.getFacture().getClient() != null) {
                clientNom = (r.getFacture().getClient().getFirstName() + " " + r.getFacture().getClient().getLastName()).trim();
            }
            if (r.getFacture().getOrdreReparation() != null) {
                numeroOrdreReparation = r.getFacture().getOrdreReparation().getNumero();
            }
        }

        return RecuResponse.builder()
                .id(r.getId())
                .numero(r.getNumero())
                .factureId(factureId)
                .numeroFacture(numeroFacture)
                .clientNom(clientNom)
                .numeroOrdreReparation(numeroOrdreReparation)
                .montant(r.getMontant())
                .modePaiement(r.getModePaiement())
                // .remarque(r.getRemarque())
                .datePaiement(r.getDatePaiement())
                .build();
    }
}
