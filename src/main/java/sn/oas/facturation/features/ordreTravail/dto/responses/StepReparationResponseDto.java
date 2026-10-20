package sn.oas.facturation.features.ordreTravail.dto.responses;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepReparationResponseDto extends BaseOtStepResponseDto {
    private String rapportReparation;
    private String dateFinReparation;
    private List<TechnicienSummaryDto> techniciensReparation = new ArrayList<>();
}
