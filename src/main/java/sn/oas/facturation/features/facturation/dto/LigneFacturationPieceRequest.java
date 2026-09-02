package sn.oas.facturation.features.facturation.dto;

import lombok.Data;

@Data
public class LigneFacturationPieceRequest {
    private Long pieceId;
    private Integer quantite;
    private Integer prix;
    private Boolean isCustom;
    private String designationPds;
}
