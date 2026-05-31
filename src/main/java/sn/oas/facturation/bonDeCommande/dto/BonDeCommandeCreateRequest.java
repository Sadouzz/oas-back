package sn.oas.facturation.bonDeCommande.dto;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class BonDeCommandeCreateRequest {

    @NotNull(message = "Le fournisseur est obligatoire")
    private Long fournisseurId;

    private Long vehiculeId;

    private Boolean tvaApplicable;

    private String observation;

    @NotEmpty(message = "Le bon de commande doit contenir au moins une ligne")
    @Valid
    private List<LigneBonDeCommandeRequest> lignes;
}