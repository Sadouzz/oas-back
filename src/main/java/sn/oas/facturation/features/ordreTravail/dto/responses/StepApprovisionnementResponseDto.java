package sn.oas.facturation.features.ordreTravail.dto.responses;

import lombok.*;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepApprovisionnementResponseDto.BonCommandeSummaryDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepApprovisionnementResponseDto.PieceApprovisionnementDto;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepApprovisionnementResponseDto extends BaseOtStepResponseDto {

    private Boolean hasBonDeCommande = false;
    private List<BonCommandeSummaryDto> bonsDeCommande = new ArrayList<>();
    private String proformaNumero;
    private List<PieceApprovisionnementDto> piecesManquantesProforma = new ArrayList<>();
}
