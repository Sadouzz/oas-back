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
public class ClotureStepDto {
    private Long ordreReparationId;
    private LocalDateTime dateCloture;
    private Integer noteSatisfactionClient;
    private Integer dureeGarantieMois;
    private String commentairesFinaux;
}
