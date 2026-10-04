package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;

@Data
public class LignePieceOrdreDto {
    private Long id;
    private Integer quantite;
    private Integer prix;
    private Boolean isCustom;
    private String designationPds;
    private Long pieceId; 
    private PieceSummaryDto piece; 
}
