package sn.oas.facturation.features.ordreTravail.dto.responses;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepAssignationResponseDto extends BaseOtStepResponseDto {
    private List<TechnicienSummaryDto> techniciens = new ArrayList<>();
}
