package sn.oas.facturation.avoirTTC.dto;

import lombok.Builder;
import lombok.Data;
import sn.oas.facturation.facturation.dto.LigneFacturationMainDoeuvreResponse;
import sn.oas.facturation.facturation.dto.LigneFacturationPieceResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AvoirTTCResponse {
    private Long id;
    private String numero;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private BigDecimal montantTimbre;
    private BigDecimal montantTotal;
    
    private Long agentId;
    private String agentNom;
    private String remarque;
    private Double kilometrage;

    private Long clientId;
    private String clientNom;
    
    private Long vehiculeId;
    private String immatriculation;
    private String numeroChassis;
    private String marque;
    private String modele;
    private Integer annee;

    private String numeroBonDeCommande;

    private List<LigneFacturationPieceResponse> lignesPieces;
    private List<LigneFacturationMainDoeuvreResponse> lignesMainDoeuvres;
}
