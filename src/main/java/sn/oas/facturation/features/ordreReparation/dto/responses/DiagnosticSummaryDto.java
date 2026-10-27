package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.ordreReparation.dto.responses.AdditionalStubs.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticSummaryDto {
    private Long id;
    private String statut;
    private String pannesDetectees;
    private String observations;
    private String recommandations;
    private Integer kilometrage;
    private Long technicienId;
    private String technicienNom;
    private String dateDebut;
    private String dateFin;
    private List<RemarqueDiagnosticDto> remarques;
    private List<PieceJointeDiagnosticDto> piecesJointes;
}
