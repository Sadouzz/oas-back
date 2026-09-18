package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BonSortieStepDto {
    private Long ordreReparationId;
    private Long clientId;
    private Long vehiculeId;
    private List<LigneBonSortieStepDto> lignesPieces;
    private String remarque;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LigneBonSortieStepDto {
        private Long pieceId;
        private Integer quantite;
        private BigDecimal prix;
        private Boolean isCustom;
        private String designationPds;
    }
}
