package sn.oas.facturation.features.bonDeCommande.dto;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Data
public class BonDeCommandeCreateRequest {

    private Long fournisseurId;

    private Long vehiculeId;

    private Boolean tvaApplicable;

    private String observation;

    @NotEmpty(message = "Le bon de commande doit contenir au moins une ligne")
    @Valid
    private List<LigneBonDeCommandeRequest> lignes;
}