package sn.oas.facturation.ordreReparation.dto;

import lombok.Data;
import java.time.LocalDateTime;
import sn.oas.facturation.ordreReparation.data.enums.StatutOrdreReparation;

@Data
public class OrdreReparationRequest {
    private String numero;
    private String descriptionTravaux;
    private String travauxDemandes;
    private String listeReception;
    private String listeDefauts;
    private LocalDateTime dateSortie;
    private Long vehiculeId;
    private StatutOrdreReparation statut;
    private java.util.List<LigneOrdreReparationPieceRequest> lignesPieces;
    private java.util.List<LigneOrdreReparationMainDoeuvreRequest> lignesMainDoeuvres;
}
