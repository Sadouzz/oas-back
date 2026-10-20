package sn.oas.facturation.features.ordreTravail.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.ordreReparation.dto.responses.BonDeSortieSummaryDto;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepBonSortieResponseDto extends BaseOtStepResponseDto {
    private Boolean hasBonDeSortie = false;
    private BonDeSortieSummaryDto bonDeSortie;
    private String proformaNumero;
    private List<PieceBonDeSortieDto> piecesManquantesProforma = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PieceBonDeSortieDto {
        private Long ligneId;
        private Long pieceId;
        private String reference;
        private String designation;

        private Integer quantiteDemandee; // Quantité requise dans le proforma
        private Double stockMagasin; // Quantité actuellement en stock magasin
        private Double stockAtelier; // Quantité en stock atelier
        private Double quantiteASortir; 
        private Double quantiteSortie; 
        private Double prixUnitaire;
    }
}
