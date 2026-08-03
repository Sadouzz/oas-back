package sn.oas.facturation.marketplace.dto;

public record ProduitRequest(
        String nom,
        String description,
        Double prix,
        String mediaUrl,
        Boolean disponible) {
}
