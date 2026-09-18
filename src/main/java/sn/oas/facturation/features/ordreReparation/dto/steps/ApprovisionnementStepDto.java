package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovisionnementStepDto {
    private Long ordreReparationId;
    private Long fournisseurId;
    private List<LigneCommandeStepDto> lignesCommande;
    private LocalDateTime dateLivraisonPrevue;
    private String remarque;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LigneCommandeStepDto {
        private Long pieceId;
        private Integer quantiteCommandee;
        private BigDecimal prixUnitaire;
    }
}
