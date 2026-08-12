package sn.oas.facturation.recu.dto;

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
    private String remarque;
    private LocalDateTime datePaiement;

    private String clientNom;
    private String numeroFacture;
    private String numeroOrdreReparation;
}
