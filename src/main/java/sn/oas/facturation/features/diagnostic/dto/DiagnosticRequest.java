package sn.oas.facturation.features.diagnostic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.diagnostic.data.enums.StatutDiagnostic;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticRequest {

    private Long ordreReparationId;
    private Long technicienId;
    private List<Long> technicienIds;
    private String observations;
    private String pannesDetectees;
    private String recommandations;
    private Integer kilometrage;
    private StatutDiagnostic statut;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private List<PieceJointeDiagnosticRequest> piecesJointes;
    private List<RemarqueRequest> remarques;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RemarqueRequest {
        private String contenu;
        private Long technicienId;
    }
}
