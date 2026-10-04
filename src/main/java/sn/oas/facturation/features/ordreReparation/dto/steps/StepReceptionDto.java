package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneReceptionOrdre;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneTravailOrdre;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepReceptionDto extends BaseStepDto {
    private String listeDefauts;
    private List<LigneTravailOrdre> lignesTravaux;
    private List<LigneReceptionOrdre> lignesReception;
}
