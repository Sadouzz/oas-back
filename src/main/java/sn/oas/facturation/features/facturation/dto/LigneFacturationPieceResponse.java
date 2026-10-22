package sn.oas.facturation.features.facturation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LigneFacturationPieceResponse {
    private Long id;
    private Long pieceId;
    private String designationPiece;
    private Boolean isCustom;
    private String designationPds;
    private String referencePiece;
    private String depotNom;
    private Integer quantite;
    private Integer prix;
    private Integer montantTotal;
    private Double tauxRemise;
    private Integer quantiteCommandee;
    private Integer quantiteRecueTotale;
    private Integer quantiteRestante;
}
