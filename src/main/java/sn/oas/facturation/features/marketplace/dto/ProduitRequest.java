package sn.oas.facturation.features.marketplace.dto;

public record ProduitRequest(
        String nom,
        String description,
        Double prix,
        String mediaUrl,
        Boolean disponible) {
}
