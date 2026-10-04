package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepAssignationDto extends BaseStepDto {
    private List<Long> techniciensIds;
    private String dateDebutPrevue;
    private Double tempsEstimeHeures;
}
