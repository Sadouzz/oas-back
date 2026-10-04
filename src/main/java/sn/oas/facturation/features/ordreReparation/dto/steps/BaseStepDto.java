package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.Data;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;

@Data
public class BaseStepDto {
    private Long ordreId;
    private String numero;
    private String descriptionTravaux;
    private Long vehiculeId;
    private StatutOrdreReparation statut;
}
