package sn.oas.facturation.features.ordreTravail.dto.responses;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sn.oas.facturation.features.ordreReparation.dto.responses.FactureSummaryDto;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepPaiementResponseDto extends BaseOtStepResponseDto {
    private FactureSummaryDto facture;
}
