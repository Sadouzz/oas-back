package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;
import sn.oas.facturation.features.ordreReparation.dto.responses.AdditionalStubs.*;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepReparationResponseDto extends BaseStepResponseDto {
    private String rapportReparation;
    private String dateFinReparation;
    private List<TechnicienDto> techniciensReparation;
}
