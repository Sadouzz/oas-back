package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepPaiementDto extends BaseStepDto {
    private Double montantPaye;
    private String methodePaiement;
}
