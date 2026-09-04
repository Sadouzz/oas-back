package sn.oas.facturation.features.bonDeSortie.dto;

import lombok.Builder;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.bonDeSortie.data.enums.StatutBon;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record BonDeSortieListResponse(
        Long id,
        String reference,
        LocalDateTime date,
        StatutBon statut,
        String remarque,
        // LocalDateTime dateValidation,
        ClientSummary client,
        VehiculeSummary vehicule,
        AgentSummary agentEmetteur,
        // AgentSummary agentValidateur,
        // Long ordreReparationId,
        List<LigneSummary> lignesBonDeSortiePieces
) {
    public record ClientSummary(Long id, String firstName, String lastName, String phone) {}
    public record VehiculeSummary(Long id, String immatriculation, String marque, String modele) {}
    public record AgentSummary(
            Long id,
            // String username,
            String firstName,
            String lastName
    ) {}
    public record PieceSummary(
            Long id,
            String reference,
            String designation
            // Double prix
    ) {}
    public record LigneSummary(
            Long id,
            PieceSummary piece,
            Integer quantite
            // Integer prix
    ) {}

    public static BonDeSortieListResponse from(BonDeSortie b) {
        if (b == null) return null;

        ClientSummary clientSummary = null;
        if (b.getClient() != null) {
            clientSummary = new ClientSummary(
                    b.getClient().getId(),
                    b.getClient().getFirstName(),
                    b.getClient().getLastName(),
                    b.getClient().getPhone()
            );
        }

        VehiculeSummary vehiculeSummary = null;
        if (b.getVehicule() != null) {
            vehiculeSummary = new VehiculeSummary(
                    b.getVehicule().getId(),
                    b.getVehicule().getImmatriculation(),
                    b.getVehicule().getMarque(),
                    b.getVehicule().getModele()
            );
        }

        AgentSummary emetteur = null;
        if (b.getAgentEmetteur() != null) {
            emetteur = new AgentSummary(
                    b.getAgentEmetteur().getId(),
                    // b.getAgentEmetteur().getUsername(),
                    b.getAgentEmetteur().getFirstName(),
                    b.getAgentEmetteur().getLastName()
            );
        }

        // AgentSummary validateur = null;
        // if (b.getAgentValidateur() != null) {
        //     validateur = new AgentSummary(
        //             b.getAgentValidateur().getId(),
        //             // b.getAgentValidateur().getUsername(),
        //             b.getAgentValidateur().getFirstName(),
        //             b.getAgentValidateur().getLastName()
        //     );
        // }

        List<LigneSummary> lignes = b.getLignesBonDeSortiePieces() == null ? List.of() :
                b.getLignesBonDeSortiePieces().stream().map(l -> {
                    PieceSummary pSummary = null;
                    if (l.getPiece() != null) {
                        pSummary = new PieceSummary(
                                l.getPiece().getId(),
                                l.getPiece().getReference(),
                                l.getPiece().getDesignation()
                                // l.getPiece().getPrixUnitaire()
                        );
                    }
                    return new LigneSummary(
                            l.getId(),
                            pSummary,
                            l.getQuantite()
                            // l.getPrix()
                    );
                }).toList();

        return BonDeSortieListResponse.builder()
                .id(b.getId())
                .reference(b.getReference())
                .date(b.getDate())
                .statut(b.getStatut())
                .remarque(b.getRemarque())
                // .dateValidation(b.getDateValidation())
                .client(clientSummary)
                .vehicule(vehiculeSummary)
                .agentEmetteur(emetteur)
                // .agentValidateur(validateur)
                // .ordreReparationId(b.getOrdreReparation() != null ? b.getOrdreReparation().getId() : null)
                .lignesBonDeSortiePieces(lignes)
                .build();
    }
}
