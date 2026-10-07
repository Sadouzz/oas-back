package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepApprovisionnementResponseDto extends BaseStepResponseDto {

    private Boolean hasBonDeCommande = false;
    private List<BonCommandeSummaryDto> bonsDeCommande = new ArrayList<>();

    private Long proformaId;
    private String proformaNumero;
    private List<PieceApprovisionnementDto> piecesManquantesProforma = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BonCommandeSummaryDto {
        private Long id;
        private String numero;
        private String reference;
        private String fournisseurNom;
        private Double montantTotal;
        private String statut;
        private LocalDateTime dateCommande;
        private Integer nombrePieces;
        @Builder.Default
        private List<PieceBonCommandeDto> pieces = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PieceBonCommandeDto {
        private Long ligneId;
        private Long pieceId;
        private String reference;
        private String designation;
        private Integer quantite;
        private Integer quantiteRecue;
        private Double prixUnitaire;
        private Double montantTotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PieceApprovisionnementDto {
        private Long ligneId;
        private Long pieceId;
        private String reference;
        private String designation;
        private String type; // "PDP", "PDG", "PDS"
        private Boolean isCustom; // true si pièce sur commande personnalisée
        private Integer quantiteDemandee; // Quantité requise dans le proforma
        private Double stockMagasin; // Quantité actuellement en stock magasin
        private Double stockAtelier; // Quantité en stock atelier
        private Double quantiteManquante; // Manque en stock (valeur positive si déficit, 0 sinon)
        private Double differenceStock; // Stock magasin - Quantité demandée
        private Boolean isManquant; // true si stockActuel < quantiteDemandee
        private Double prixUnitaire;
        private Double montantTotal;
        private Integer quantiteCommande;
        private Integer quantiteRecue;
    }
}
