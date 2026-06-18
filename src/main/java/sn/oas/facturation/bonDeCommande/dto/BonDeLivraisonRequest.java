package sn.oas.facturation.bonDeCommande.dto;

import lombok.Data;

import java.util.List;

@Data
public class BonDeLivraisonRequest {

    private List<LigneLivraison> lignes;

    @Data
    public static class LigneLivraison {
        private Long ligneId;
        private Integer quantiteRecue;
    }
}
