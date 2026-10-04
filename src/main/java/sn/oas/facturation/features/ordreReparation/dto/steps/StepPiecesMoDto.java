package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepPiecesMoDto extends BaseStepDto {
    private List<LignePieceDto> lignesPieces;
    private List<LigneMoDto> lignesMainDoeuvres;
}
