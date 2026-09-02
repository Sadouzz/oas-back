package sn.oas.facturation.features.marketplace.dto;

public record DemandeProduitRequest(
        Long produitId,
        Integer quantite,
        String message) {
}
