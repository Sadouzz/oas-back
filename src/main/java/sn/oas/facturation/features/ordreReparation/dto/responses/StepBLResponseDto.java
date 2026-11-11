package sn.oas.facturation.features.ordreReparation.dto.responses;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.proforma.dto.ProformaDetailsResponseDto.LigneMainDoeuvreResponseDto;
import sn.oas.facturation.features.proforma.dto.ProformaDetailsResponseDto.LignePieceResponseDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class StepBLResponseDto extends BaseStepResponseDto {

    private Integer etape;
    private Long clientId;
    private String clientNom;
    private String clientTelephone;

    private String immatriculation;
    private String marque;
    private String modele;

    @JsonProperty("hasBonDeLivraison")
    @JsonAlias({"hasBL", "hasBonLivraison"})
    private boolean hasBonDeLivraison;

    @JsonProperty("bonDeLivraison")
    @JsonAlias({"bl"})
    private BonDeLivraisonSummaryDto bonDeLivraison;

    private Double montantHT = 0.0;
    private Double montantTVA = 0.0;
    private Double montantTTC = 0.0;
    private Double montantTotal = 0.0;
    private Boolean tvaApplicable = true;
    private Double tvaRate;

    @JsonProperty("lignesPieces")
    @JsonAlias({"pieces", "lignesPiecesBL"})
    private List<LignePieceResponseDto> lignesPieces = new ArrayList<>();

    @JsonProperty("lignesMainDoeuvres")
    @JsonAlias({"mainDoeuvres", "lignesMainDoeuvresBL"})
    private List<LigneMainDoeuvreResponseDto> lignesMainDoeuvres = new ArrayList<>();

    // Rétro-compatibilité Paiement / Facture
    private FactureSummaryDto facture;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BonDeLivraisonSummaryDto {
        private Long id;
        private String numero;
        private LocalDateTime dateCreation;
        private String statut;
        private String remarque;
        private Double montantHT;
        private Double montantTVA;
        private Double montantTTC;
        private Boolean tvaApplicable;
        private Long factureId;
        private String factureNumero;
    }
}

