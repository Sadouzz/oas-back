package sn.oas.facturation.noteDePrix.dto;

import lombok.Data;
import sn.oas.facturation.facturation.dto.LigneFacturationMainDoeuvreRequest;
import sn.oas.facturation.facturation.dto.LigneFacturationPieceRequest;

import java.util.List;

@Data
public class NoteDePrixRequest {
    private Long ordreReparationId;
    private Double kilometrage;
    private String remarque;
    private List<LigneFacturationPieceRequest> lignesPieces;
    private List<LigneFacturationMainDoeuvreRequest> lignesMainDoeuvres;
}
