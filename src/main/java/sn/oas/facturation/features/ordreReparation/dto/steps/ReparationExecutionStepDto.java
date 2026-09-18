package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReparationExecutionStepDto {
    private Long ordreReparationId;
    private LocalDateTime dateFinTravaux;
    private Boolean controleQualiteValide;
    private String commentairesEssai;
    private Integer kilometrageApresEssai;
}
