package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaiementStepDto {
    private Long ordreReparationId;
    private Long factureId;
    private BigDecimal montantRegle;
    private ModePaiement modePaiement;
    private String referenceTransaction;
    private LocalDateTime datePaiement;
    private BigDecimal soldeRestantDu;
}
