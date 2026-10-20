package sn.oas.facturation.features.ordreTravail.dto.responses;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sn.oas.facturation.features.ordreReparation.dto.responses.BonDeSortieSummaryDto;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepBonSortieResponseDto extends BaseOtStepResponseDto {
    private BonDeSortieSummaryDto bonDeSortie;
}
