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
public class DiagnosticResponse {

    private Long id;
    private Long ordreReparationId;
    private String ordreReparationNumero;

    private DiagnosticListResponse.VehiculeSummary vehicule;

    private Long technicienId;
    private String technicienNom;
    private List<Long> technicienIds;
    private List<DiagnosticListResponse.TechnicienSummary> techniciens;

    private String observations;
    private String pannesDetectees;
    private String recommandations;
    private Integer kilometrage;
    private StatutDiagnostic statut;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private List<PieceJointeDiagnosticResponse> piecesJointes;
    private List<RemarqueDiagnosticResponse> remarques;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
