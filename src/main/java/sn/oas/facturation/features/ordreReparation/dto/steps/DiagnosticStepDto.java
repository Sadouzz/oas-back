package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.diagnostic.data.enums.TypePieceJointe;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticStepDto {
    private Long ordreReparationId;
    private List<Long> technicienIds;
    private String listeDefauts;
    private List<RemarqueDiagnosticDto> remarques;
    private List<PieceJointeDiagnosticDto> piecesJointes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RemarqueDiagnosticDto {
        private String contenu;
        private Long technicienId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PieceJointeDiagnosticDto {
        private String url;
        private TypePieceJointe type; // PHOTO, PDF
        private String remarque;
    }
}
