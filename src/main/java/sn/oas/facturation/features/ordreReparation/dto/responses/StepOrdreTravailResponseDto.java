package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sn.oas.facturation.features.ordreTravail.dto.OrdreTravailResponseDto;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepOrdreTravailResponseDto extends BaseStepResponseDto {
    private Integer etape;
    private Long ordreTravailId;
    private String ordreTravailNumero;
    private String ordreTravailStatut;
    private Integer ordreTravailEtape;
    private VehiculeHeaderDto vehicule;
    private ClientHeaderDto client;
    private ProformaSummaryDto proforma;
    private OrdreTravailResponseDto ordreTravail;
    private LocalDateTime updatedAt;
    private LocalDateTime dateCreation;
}
