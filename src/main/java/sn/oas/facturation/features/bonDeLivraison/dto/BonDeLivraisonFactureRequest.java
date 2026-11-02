package sn.oas.facturation.features.bonDeLivraison.dto;

import lombok.Data;

@Data
public class BonDeLivraisonFactureRequest {
    private String modePaiement;
    private String remarque;
}
