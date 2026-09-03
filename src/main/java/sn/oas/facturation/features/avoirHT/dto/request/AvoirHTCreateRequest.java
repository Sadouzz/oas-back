package sn.oas.facturation.features.avoirHT.dto.request;

import lombok.Data;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreRequest;
import sn.oas.facturation.features.facturation.dto.LigneFacturationPieceRequest;

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
