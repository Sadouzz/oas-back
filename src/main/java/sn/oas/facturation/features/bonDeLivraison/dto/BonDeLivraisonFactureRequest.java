package sn.oas.facturation.features.bonDeLivraison.dto;

import lombok.Data;

@Data
public class BonDeLivraisonFactureRequest {
    private Boolean appliquerTVA;
    private String remarque;
}
