package sn.oas.facturation.features.avoirTTC.dto;

import lombok.Data;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreRequest;
import sn.oas.facturation.features.facturation.dto.LigneFacturationPieceRequest;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AvoirTTCCreateRequest {
    private Long clientId;
    private Long vehiculeId;
    private Double kilometrage;
    private String remarque;
    private Boolean appliquerTVA; // true = 18% TVA
    private BigDecimal montantTimbre;
    private List<LigneFacturationPieceRequest> lignesPieces;
    private List<LigneFacturationMainDoeuvreRequest> lignesMainDoeuvres;
}
