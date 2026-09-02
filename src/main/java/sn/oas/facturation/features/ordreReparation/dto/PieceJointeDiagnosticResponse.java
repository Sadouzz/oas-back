package sn.oas.facturation.features.ordreReparation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.ordreReparation.data.enums.TypePieceJointe;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PieceJointeDiagnosticResponse {
    private Long id;
    private Long ordreReparationId;
    private String url;
    private TypePieceJointe type;
    private String remarque;
    private String technicienNom;
    private LocalDateTime createdAt;
}

