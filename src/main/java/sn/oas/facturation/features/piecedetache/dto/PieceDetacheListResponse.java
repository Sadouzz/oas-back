package sn.oas.facturation.features.piecedetache.dto;

import lombok.Builder;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.data.enums.StatutPiece;
import sn.oas.facturation.features.piecedetache.data.enums.TypePiece;

import java.time.LocalDateTime;

@Builder
public record PieceDetacheListResponse(
        Long id,
        TypePiece type,
        String numero,
        String reference,
        String designation,
        CategorieSummary categorie,
        DepotSummary depot,
        Double prixUnitaire,
        Double prixGros,
        Double pourcentage,
        Double stockMagasin,
        Double stockAtelier,
        Double qteReelle,
        Double seuilMinimum,
        StatutPiece statut,
        boolean estUtilise
        // LocalDateTime createdAt
) {
    public record DepotSummary(Long id, String nom) {}

    public record CategorieSummary(Long id, String nom, DepotSummary depot) {
        public CategorieSummary(Long id, String nom) {
            this(id, nom, null);
        }
    }

    public static PieceDetacheListResponse from(PieceDetache p) {
        if (p == null) return null;

        DepotSummary depotSummary = null;
        if (p.getDepot() != null) {
            depotSummary = new DepotSummary(
                    p.getDepot().getId(),
                    p.getDepot().getNom()
            );
        } else if (p.getCategorie() != null && p.getCategorie().getDepots() != null && !p.getCategorie().getDepots().isEmpty()) {
            depotSummary = new DepotSummary(
                    p.getCategorie().getDepots().get(0).getId(),
                    p.getCategorie().getDepots().get(0).getNom()
            );
        }

        CategorieSummary categorieSummary = null;
        if (p.getCategorie() != null) {
            categorieSummary = new CategorieSummary(
                    p.getCategorie().getId(),
                    p.getCategorie().getNom(),
                    depotSummary
            );
        }

        Double qteReelle = null;
        Double seuilMinimum = null;
        if (p instanceof PDP pdp) {
            qteReelle = pdp.getQteReelle();
            seuilMinimum = pdp.getSeuilMinimum();
        }

        return PieceDetacheListResponse.builder()
                .id(p.getId())
                .type(p.getType())
                .numero(p.getNumero())
                .reference(p.getReference())
                .designation(p.getDesignation())
                .categorie(categorieSummary)
                .depot(depotSummary)
                .prixUnitaire(p.getPrixUnitaire())
                .prixGros(p.getPrixGros())
                .pourcentage(p.getPourcentage())
                .stockMagasin(p.getStockMagasin())
                .stockAtelier(p.getStockAtelier())
                .qteReelle(qteReelle)
                .seuilMinimum(seuilMinimum)
                .statut(p.getStatut())
                .estUtilise(p.isEstUtilise())
                // .createdAt(p.getCreatedAt())
                .build();
    }
}
