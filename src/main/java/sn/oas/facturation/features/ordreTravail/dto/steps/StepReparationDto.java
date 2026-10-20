package sn.oas.facturation.features.ordreTravail.dto.steps;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepReparationDto extends BaseOtStepDto {
    private String rapportReparation;
    private String dateFinReparation;
}
