package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LignePieceDto {
    private Long pieceId;
    private Boolean isCustom;
    private String designationPds;
    private Integer quantite;
    private BigDecimal prixUnitaire;
}
