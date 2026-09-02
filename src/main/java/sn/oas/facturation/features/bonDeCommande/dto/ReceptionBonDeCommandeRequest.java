package sn.oas.facturation.features.bonDeCommande.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReceptionBonDeCommandeRequest {

    private List<LigneReception> lignes;

    @Data
    public static class LigneReception {
        private Long ligneId;
        private Integer quantiteRecue;
    }
}
