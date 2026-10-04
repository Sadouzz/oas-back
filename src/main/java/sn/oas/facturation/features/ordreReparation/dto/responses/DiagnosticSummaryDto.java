package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;
import sn.oas.facturation.features.ordreReparation.dto.responses.AdditionalStubs.*;
import java.util.List;

@Data
public class DiagnosticSummaryDto {
    private Long id;
    private String statut;
    private String pannesDetectees;
    private List<RemarqueDiagnosticDto> remarques;
    private List<PieceJointeDiagnosticDto> piecesJointes;
}
