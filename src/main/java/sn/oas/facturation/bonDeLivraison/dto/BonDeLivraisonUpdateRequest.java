package sn.oas.facturation.bonDeLivraison.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sn.oas.facturation.facturation.dto.LigneFacturationMainDoeuvreRequest;
import sn.oas.facturation.facturation.dto.LigneFacturationPieceRequest;

import java.util.List;

@Data
public class BonDeLivraisonUpdateRequest {
    
    private Long bonDeCommandeId;
    
    @NotNull(message = "Le kilométrage est obligatoire")
    private Double kilometrage;

    private String remarque;

    private Boolean paye;

    @Valid
    private List<LigneFacturationPieceRequest> lignesPieces;

    @Valid
    private List<LigneFacturationMainDoeuvreRequest> lignesMainDoeuvres;
}
