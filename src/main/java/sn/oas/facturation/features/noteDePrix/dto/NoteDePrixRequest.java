package sn.oas.facturation.features.noteDePrix.dto;

import lombok.Data;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreRequest;
import sn.oas.facturation.features.facturation.dto.LigneFacturationPieceRequest;

import java.math.BigDecimal;
import java.util.List;

@Data
public class NoteDePrixRequest {
    private Long clientId;
    private Long vehiculeId;
    private Long ordreReparationId;
    private Double kilometrage;
    private String remarque;
    private String numeroBonDeCommande;
    private BigDecimal montantAutre;
    private BigDecimal montantPaye;
    private BigDecimal resteAPayer;
    private String statutPaiement;
    private String modePaiement;
    private List<LigneFacturationPieceRequest> lignesPieces;
    private List<LigneFacturationMainDoeuvreRequest> lignesMainDoeuvres;
}
