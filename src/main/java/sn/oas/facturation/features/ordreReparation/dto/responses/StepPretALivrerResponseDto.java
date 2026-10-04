package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepPretALivrerResponseDto extends BaseStepResponseDto {
    private String remarquesPreparation;
}
