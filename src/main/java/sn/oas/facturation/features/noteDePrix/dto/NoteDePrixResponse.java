package sn.oas.facturation.features.noteDePrix.dto;

import lombok.Builder;
import lombok.Data;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreResponse;
import sn.oas.facturation.features.facturation.dto.LigneFacturationPieceResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class NoteDePrixResponse {
    private Long id;
    private String numero;
    private LocalDateTime dateCreation;
    private BigDecimal montantHT;
    private BigDecimal montantTotal;
    private BigDecimal montantPaye;
    private BigDecimal resteAPayer;
    private String statutPaiement;
    private String modePaiement;
    private String numeroBonDeCommande;
    private BigDecimal montantAutre;

    private Long agentId;
    private String agentNom;
    private Long clientId;
    private String clientNom;
    private Long vehiculeId;
    private String vehiculeImmatriculation;
    private String immatriculation;
    private String marque;
    private String modele;
    private String numeroChassis;
    private Double kilometrage;
    private String remarque;
    private String statut;
    private Long ordreReparationId;
    private String numeroOrdreReparation;
    
    private List<LigneFacturationPieceResponse> lignesPieces;
    private List<LigneFacturationMainDoeuvreResponse> lignesMainDoeuvres;
}
