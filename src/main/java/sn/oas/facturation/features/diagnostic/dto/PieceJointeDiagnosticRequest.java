package sn.oas.facturation.features.diagnostic.dto;

import lombok.Data;
import sn.oas.facturation.features.diagnostic.data.enums.TypePieceJointe;

@Data
public class PieceJointeDiagnosticRequest {
    private String url;
    private TypePieceJointe type;
    private String remarque;
}
