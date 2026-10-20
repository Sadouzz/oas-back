package sn.oas.facturation.features.ordreTravail.dto.responses;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sn.oas.facturation.features.ordreReparation.dto.responses.ClientHeaderDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.ProformaSummaryDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.VehiculeHeaderDto;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepRecapitulatifResponseDto extends BaseOtStepResponseDto {
    private ProformaSummaryDto proforma;
    private VehiculeHeaderDto vehicule;
    private ClientHeaderDto client;
    private String ordreReparationNumero;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
