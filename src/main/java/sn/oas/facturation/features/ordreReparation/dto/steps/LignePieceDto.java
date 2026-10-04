package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.Data;

@Data
public class LignePieceDto {
    private Long pieceId;
    private Integer quantite;
    private Double prix;
    private Boolean isCustom;
    private String designationPds;
}
