package sn.oas.facturation.marketplace.dto;

public record DemandeProduitRequest(
        Long produitId,
        Integer quantite,
        String message) {
}
