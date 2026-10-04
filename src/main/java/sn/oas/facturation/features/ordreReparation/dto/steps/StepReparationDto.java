package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepReparationDto extends BaseStepDto {
    private String rapportReparation;
    private List<LignePieceDto> piecesReellementUtilisees;
    private String dateFinReparation;
}
