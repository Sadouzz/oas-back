package sn.oas.facturation.features.recu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecuRequest {
    private Long factureId;
    private BigDecimal montant;
    private String modePaiement;
    private String remarque;
}
