package sn.oas.facturation.facturation.dto;

import lombok.Data;

@Data
public class LigneFacturationPieceRequest {
    private Long pieceId;
    private Integer quantite;
    private Integer prix;
}
