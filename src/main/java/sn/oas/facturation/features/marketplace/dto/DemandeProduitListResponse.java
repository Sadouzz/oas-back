package sn.oas.facturation.features.marketplace.dto;

import lombok.Builder;
import sn.oas.facturation.features.marketplace.data.entity.DemandeProduit;
import sn.oas.facturation.features.marketplace.data.enums.StatutDemandeProduit;

import java.time.LocalDateTime;

@Builder
public record DemandeProduitListResponse(
        Long id,
        String numero,
        ClientSummary client,
        ProduitSummary produit,
        Integer quantite,
        String message,
        StatutDemandeProduit statut,
        LocalDateTime dateCreation
) {
    public record ClientSummary(Long id, String firstName, String lastName, String phone, String email) {}
    public record ProduitSummary(Long id, String nom, Double prix, String mediaUrl) {}

    public static DemandeProduitListResponse from(DemandeProduit d) {
        if (d == null) return null;

        ClientSummary clientSummary = null;
        if (d.getClient() != null) {
            clientSummary = new ClientSummary(
                    d.getClient().getId(),
                    d.getClient().getFirstName(),
                    d.getClient().getLastName(),
                    d.getClient().getPhone(),
                    d.getClient().getEmail()
            );
        }

        ProduitSummary produitSummary = null;
        if (d.getProduit() != null) {
            produitSummary = new ProduitSummary(
                    d.getProduit().getId(),
                    d.getProduit().getNom(),
                    d.getProduit().getPrix(),
                    d.getProduit().getMediaUrl()
            );
        }

        return DemandeProduitListResponse.builder()
                .id(d.getId())
                .numero(d.getNumero())
                .client(clientSummary)
                .produit(produitSummary)
                .quantite(d.getQuantite())
                .message(d.getMessage())
                .statut(d.getStatut())
                .dateCreation(d.getDateCreation())
                .build();
    }
}
