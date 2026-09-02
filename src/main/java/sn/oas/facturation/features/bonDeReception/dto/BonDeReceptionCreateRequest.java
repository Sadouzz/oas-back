package sn.oas.facturation.features.bonDeReception.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreRequest;
import sn.oas.facturation.features.facturation.dto.LigneFacturationPieceRequest;

import java.util.List;

@Data
public class BonDeReceptionCreateRequest {
    
    private Long bonDeCommandeId;
    
    @NotNull(message = "Le kilométrage est obligatoire")
    private Double kilometrage;

    private String remarque;

    @Valid
    private List<LigneFacturationPieceRequest> lignesPieces;

    @Valid
    private List<LigneFacturationMainDoeuvreRequest> lignesMainDoeuvres;
}
