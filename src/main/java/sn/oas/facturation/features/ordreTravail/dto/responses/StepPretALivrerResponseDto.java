package sn.oas.facturation.features.ordreTravail.dto.responses;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepPretALivrerResponseDto extends BaseOtStepResponseDto {
    private String remarquesPreparation;
}
