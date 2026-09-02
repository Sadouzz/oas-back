package sn.oas.facturation.features.facturation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LigneFacturationPieceResponse {
    private Long id;
    private Long pieceId;
    private String designationPiece;
    private Integer quantite;
    private Integer prix;
    private Integer montantTotal;
}
