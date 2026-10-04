package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepLivraisonDto extends BaseStepDto {
    private String dateSortie;
    private Double kilometrageSortie;
    private String remarquesClient;
}
