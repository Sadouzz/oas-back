package sn.oas.facturation.facture.dto;

import lombok.Builder;
import lombok.Data;
import sn.oas.facturation.facturation.dto.LigneFacturationMainDoeuvreResponse;
import sn.oas.facturation.facturation.dto.LigneFacturationPieceResponse;
import sn.oas.facturation.recu.dto.RecuResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class FactureResponse {
    private Long id;
    private String numero;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private BigDecimal montantTimbre;
    private BigDecimal montantAutre;
    private BigDecimal montantTotal;
    private BigDecimal montantPaye;
    private BigDecimal resteAPayer;
    private sn.oas.facturation.facture.data.enums.StatutPaiement statutPaiement;

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

    private Long ordreReparationId;
    private String numeroOrdreReparation;

    private List<LigneFacturationPieceResponse> lignesPieces;
    private List<LigneFacturationMainDoeuvreResponse> lignesMainDoeuvres;
    private List<RecuResponse> recus;
}
