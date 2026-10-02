package sn.oas.facturation.features.client.dto;


public record ClientFideleRequest(
        Integer montantRemise,
        Integer montantPlafond,
        Integer echeance,
        String ninea,
        String rccm,
        String rib) {
}
