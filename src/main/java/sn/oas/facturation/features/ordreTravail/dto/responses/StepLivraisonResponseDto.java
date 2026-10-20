package sn.oas.facturation.features.ordreTravail.dto.responses;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepLivraisonResponseDto extends BaseOtStepResponseDto {
    private String dateSortie;
    private Double kilometrageSortie;
    private String remarquesClient;
}
