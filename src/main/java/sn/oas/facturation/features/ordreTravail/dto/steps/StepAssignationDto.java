package sn.oas.facturation.features.ordreTravail.dto.steps;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepAssignationDto extends BaseOtStepDto {
    private List<Long> techniciensIds;
    private String dateDebutPrevue;
    private Double tempsEstimeHeures;
}
