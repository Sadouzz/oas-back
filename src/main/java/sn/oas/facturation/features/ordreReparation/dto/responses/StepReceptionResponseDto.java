package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sn.oas.facturation.features.ficheAtelier.data.entity.LigneDefaut;
import sn.oas.facturation.features.ficheAtelier.dto.FicheAtelierDetailsResponse;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneReceptionOrdre;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneTravailOrdre;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepReceptionResponseDto extends BaseStepResponseDto {
    private String descriptionTravaux;
    private List<LigneTravailOrdre> lignesTravaux;
    private List<LigneReceptionOrdre> lignesReception;
    private String listeDefauts;
    private List<LigneDefaut> lignesDefauts;
    private Long ficheAtelierId;
    private FicheAtelierDetailsResponse ficheAtelier;
    private VehiculeHeaderDto vehicule;
}

