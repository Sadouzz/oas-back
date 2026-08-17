package sn.oas.facturation.ordreReparation.dto;

import lombok.Data;
import sn.oas.facturation.ordreReparation.data.enums.TypePieceJointe;

@Data
public class PieceJointeDiagnosticRequest {
    private String url;
    private TypePieceJointe type;
    private String remarque;
}
