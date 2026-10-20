package sn.oas.facturation.features.ordreTravail.dto.responses;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sn.oas.facturation.features.ordreReparation.dto.responses.ClientHeaderDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.ProformaSummaryDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.VehiculeHeaderDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepApprovisionnementResponseDto.PieceApprovisionnementDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepRecapitulatifResponseDto extends BaseOtStepResponseDto {
    private ProformaSummaryDto proforma;
    private VehiculeHeaderDto vehicule;
    private ClientHeaderDto client;
    private String ordreReparationNumero;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PieceApprovisionnementDto> piecesManquantesProforma = new ArrayList<>();
}
