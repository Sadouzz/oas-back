package sn.oas.facturation.features.piecedetache.dto;

import lombok.Builder;
import sn.oas.facturation.features.piecedetache.data.entity.PieceMouvement;

import java.time.LocalDateTime;

@Builder
public record PieceMouvementListResponse(
        Long id,
        String prenom,
        String nom,
        String numDoc,
        String typeDoc,
        String numeroSerie,
        String immatriculation,
        String designation,
        String action,
        Double quantite,
        Double stockMagasin,
        Double stockAtelier,
        Double stockReel,
        LocalDateTime date
) {
    public static PieceMouvementListResponse from(PieceMouvement m) {
        if (m == null) return null;

        String designation = null;
        String refPiece = m.getNumeroSerie();

        if (m.getPiece() != null) {
            designation = m.getPiece().getDesignation();
            if (refPiece == null || refPiece.isBlank()) {
                refPiece = m.getPiece().getReference();
            }
        }

        String prenom = m.getPrenom();
        String nom = m.getNom();
        if (m.getAgent() != null) {
            if (prenom == null || prenom.isBlank()) prenom = m.getAgent().getFirstName();
            if (nom == null || nom.isBlank()) nom = m.getAgent().getLastName();
        }

        String action = m.getType() != null ? m.getType().name() : null;

        return PieceMouvementListResponse.builder()
                .id(m.getId())
                .prenom(prenom)
                .nom(nom)
                .numDoc(m.getNumDocument())
                .typeDoc(m.getTypeDocument())
                .numeroSerie(refPiece)
                .immatriculation(m.getImmatriculation())
                .designation(designation)
                .action(action)
                .quantite(m.getQuantite())
                .stockMagasin(m.getStockMagasinApres())
                .stockAtelier(m.getStockAtelierApres())
                .stockReel(m.getStockReelApres())
                .date(m.getDateOperation())
                .build();
    }
}
