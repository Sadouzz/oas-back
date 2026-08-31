package sn.oas.facturation.avoirHT.dto;

import lombok.Data;
import sn.oas.facturation.facturation.dto.LigneFacturationMainDoeuvreRequest;
import sn.oas.facturation.facturation.dto.LigneFacturationPieceRequest;

import java.util.List;

@Data
public class AvoirHTCreateRequest {
    private Long clientId;
    private Long vehiculeId;
    private Double kilometrage;
    private String remarque;
    private List<LigneFacturationPieceRequest> lignesPieces;
    private List<LigneFacturationMainDoeuvreRequest> lignesMainDoeuvres;
}
