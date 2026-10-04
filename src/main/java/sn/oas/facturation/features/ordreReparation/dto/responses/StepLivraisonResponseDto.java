package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepLivraisonResponseDto extends BaseStepResponseDto {
    private String dateSortie;
    private Double kilometrageSortie;
    private String remarquesClient;
}
