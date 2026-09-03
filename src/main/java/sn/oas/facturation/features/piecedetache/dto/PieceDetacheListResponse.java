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
        Double prix,
        Double prixUnitaire,
        Double prixGros,
        Double pourcentage,
        Double stockMagasin,
        Double stockAtelier,
        Double qteReelle,
        Double seuilMinimum,
        StatutPiece statut,
        boolean estUtilise,
        LocalDateTime createdAt
) {
    public record CategorieSummary(Long id, String nom) {}

    public static PieceDetacheListResponse from(PieceDetache p) {
        if (p == null) return null;

        CategorieSummary categorieSummary = null;
        if (p.getCategorie() != null) {
            categorieSummary = new CategorieSummary(p.getCategorie().getId(), p.getCategorie().getNom());
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
                .prix(p.getPrixUnitaire())
                .prixUnitaire(p.getPrixUnitaire())
                .prixGros(p.getPrixGros())
                .pourcentage(p.getPourcentage())
                .stockMagasin(p.getStockMagasin())
                .stockAtelier(p.getStockAtelier())
                .qteReelle(qteReelle)
                .seuilMinimum(seuilMinimum)
                .statut(p.getStatut())
                .estUtilise(p.isEstUtilise())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
