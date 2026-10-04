package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;
import sn.oas.facturation.features.ordreReparation.dto.responses.AdditionalStubs.*;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepDiagnosticResponseDto extends BaseStepResponseDto {
    private String listeDefauts;
    private List<TechnicienDto> techniciensAssocies;
    private DiagnosticSummaryDto diagnostic; 
}
