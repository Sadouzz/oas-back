package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;
import sn.oas.facturation.features.ordreReparation.dto.responses.AdditionalStubs.*;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepReceptionResponseDto extends BaseStepResponseDto {
    private String descriptionTravaux;
    private List<LigneTravailOrdreDto> lignesTravaux;
    private List<LigneReceptionOrdreDto> lignesReception;
    private VehiculeSummaryDto vehicule; 
}
