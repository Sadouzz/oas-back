 package sn.oas.facturation.features.ordreTravail.dto.steps;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sn.oas.facturation.features.ordreTravail.dto.DocumentOrdreTravailRequestDto;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepReparationDto extends BaseOtStepDto {
    private String rapportReparation;
    private String dateFinReparation;
    private List<String> avertissementsBonsDeSortie = new ArrayList<>();
    private List<DocumentOrdreTravailRequestDto> documents = new ArrayList<>();
    private List<DocumentOrdreTravailRequestDto> piecesJointes = new ArrayList<>();
}
