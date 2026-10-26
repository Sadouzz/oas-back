package sn.oas.facturation.features.client.dto;


public record ClientFideleRequest(
        Integer echeance,
        java.math.BigDecimal montantPlafondEcheance,
        String ninea,
        String rccm,
        String rib) {
}
