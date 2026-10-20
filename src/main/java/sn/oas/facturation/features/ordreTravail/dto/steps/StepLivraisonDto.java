package sn.oas.facturation.features.ordreTravail.dto.steps;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepLivraisonDto extends BaseOtStepDto {
    private String dateSortie;
    private Double kilometrageSortie;
    private String remarquesClient;
}
