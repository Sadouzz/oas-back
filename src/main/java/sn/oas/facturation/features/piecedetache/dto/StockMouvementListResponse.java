package sn.oas.facturation.features.piecedetache.dto;

import lombok.Builder;
import sn.oas.facturation.features.piecedetache.data.entity.StockMouvement;
import sn.oas.facturation.features.piecedetache.data.enums.TypeMouvement;

import java.time.LocalDateTime;

@Builder
public record StockMouvementListResponse(
        Long id,
        TypeMouvement type,
        Double quantite,
        Double stockMagasinAvant,
        Double stockAtelierAvant,
        Double stockMagasinApres,
        Double stockAtelierApres,
        Double stockReelApres,
        String prenom,
        String nom,
        String numDocument,
        String typeDocument,
        String numeroSerie,
        String immatriculation,
        String motif,
        LocalDateTime dateOperation,
        PieceSummary piece,
        AgentSummary agent
) {
    public record PieceSummary(Long id, String reference, String designation, Double prixUnitaire) {}
    public record AgentSummary(Long id, String username, String firstName, String lastName) {}

    public static StockMouvementListResponse from(StockMouvement m) {
        if (m == null) return null;

        PieceSummary pieceSummary = null;
        if (m.getPiece() != null) {
            pieceSummary = new PieceSummary(
                    m.getPiece().getId(),
                    m.getPiece().getReference(),
                    m.getPiece().getDesignation(),
                    m.getPiece().getPrixUnitaire()
            );
        }

        AgentSummary agentSummary = null;
        if (m.getAgent() != null) {
            agentSummary = new AgentSummary(
                    m.getAgent().getId(),
                    m.getAgent().getUsername(),
                    m.getAgent().getFirstName(),
                    m.getAgent().getLastName()
            );
        }

        return StockMouvementListResponse.builder()
                .id(m.getId())
                .type(m.getType())
                .quantite(m.getQuantite())
                .stockMagasinAvant(m.getStockMagasinAvant())
                .stockAtelierAvant(m.getStockAtelierAvant())
                .stockMagasinApres(m.getStockMagasinApres())
                .stockAtelierApres(m.getStockAtelierApres())
                .stockReelApres(m.getStockReelApres())
                .prenom(m.getPrenom())
                .nom(m.getNom())
                .numDocument(m.getNumDocument())
                .typeDocument(m.getTypeDocument())
                .numeroSerie(m.getNumeroSerie())
                .immatriculation(m.getImmatriculation())
                .motif(m.getMotif())
                .dateOperation(m.getDateOperation())
                .piece(pieceSummary)
                .agent(agentSummary)
                .build();
    }
}
