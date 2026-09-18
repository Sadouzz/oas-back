package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignationReparationStepDto {
    private Long ordreReparationId;
    private List<Long> technicienIds;
    private LocalDateTime dateDebutPrevue;
    private Long chefEquipeId;
}
