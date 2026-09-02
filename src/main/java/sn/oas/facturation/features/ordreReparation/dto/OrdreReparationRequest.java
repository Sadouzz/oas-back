package sn.oas.facturation.features.ordreReparation.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneReceptionOrdre;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneTravailOrdre;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;

@Data
public class OrdreReparationRequest {
    private String numero;
    private String descriptionTravaux;
    private List<LigneTravailOrdre> lignesTravaux;
    private List<LigneReceptionOrdre> lignesReception;
    private String listeDefauts;
    private LocalDateTime dateSortie;
    private Long vehiculeId;
    private StatutOrdreReparation statut;
    private java.util.List<LigneOrdreReparationPieceRequest> lignesPieces;
    private java.util.List<LigneOrdreReparationMainDoeuvreRequest> lignesMainDoeuvres;
}
