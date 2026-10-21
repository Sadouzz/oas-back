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
        LocalDateTime dateValidation,
        ClientSummary client,
        VehiculeSummary vehicule,
        AgentSummary agentEmetteur,
        AgentSummary agentValidateur,
        Long ordreReparationId,
        OrdreReparationSummary ordreReparation,
        Long ordreTravailId,
        OrdreTravailSummary ordreTravail,
        Long proformaId,
        String proformaNumero,
        List<LigneSummary> lignesBonDeSortiePieces
) {
    public record ClientSummary(Long id, String firstName, String lastName, String phone) {}
    public record VehiculeSummary(Long id, String immatriculation, String marque, String modele) {}
    public record AgentSummary(
            Long id,
            String username,
            String firstName,
            String lastName
    ) {}
    public record OrdreReparationSummary(Long id, String numero) {}
    public record OrdreTravailSummary(Long id, String numero) {}
    public record PieceSummary(
            Long id,
            String reference,
            String designation,
            Double prix
    ) {}
    public record LigneSummary(
            Long id,
            PieceSummary piece,
            Integer quantite,
            Integer prix
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
                    b.getAgentEmetteur().getUsername(),
                    b.getAgentEmetteur().getFirstName(),
                    b.getAgentEmetteur().getLastName()
            );
        }

        AgentSummary validateur = null;
        if (b.getAgentValidateur() != null) {
            validateur = new AgentSummary(
                    b.getAgentValidateur().getId(),
                    b.getAgentValidateur().getUsername(),
                    b.getAgentValidateur().getFirstName(),
                    b.getAgentValidateur().getLastName()
            );
        }

        List<LigneSummary> lignes = b.getLignesBonDeSortiePieces() == null ? List.of() :
                b.getLignesBonDeSortiePieces().stream().map(l -> {
                    PieceSummary pSummary = null;
                    if (l.getPiece() != null) {
                        pSummary = new PieceSummary(
                                l.getPiece().getId(),
                                l.getPiece().getReference(),
                                l.getPiece().getDesignation(),
                                l.getPiece().getPrixUnitaire()
                        );
                    }
                    return new LigneSummary(
                            l.getId(),
                            pSummary,
                            l.getQuantite(),
                            l.getPrix() != null ? l.getPrix() : (l.getPiece() != null && l.getPiece().getPrixUnitaire() != null ? l.getPiece().getPrixUnitaire().intValue() : 0)
                    );
                }).toList();

        OrdreReparationSummary ordreSummary = null;
        if (b.getOrdreReparation() != null) {
            ordreSummary = new OrdreReparationSummary(
                    b.getOrdreReparation().getId(),
                    b.getOrdreReparation().getNumero()
            );
        }

        OrdreTravailSummary ordreTravailSummary = null;
        if (b.getOrdreTravail() != null) {
            ordreTravailSummary = new OrdreTravailSummary(
                    b.getOrdreTravail().getId(),
                    b.getOrdreTravail().getNumero()
            );
        }

        return BonDeSortieListResponse.builder()
                .id(b.getId())
                .reference(b.getReference())
                .date(b.getDate())
                .statut(b.getStatut())
                .remarque(b.getRemarque())
                .dateValidation(b.getDateValidation())
                .client(clientSummary)
                .vehicule(vehiculeSummary)
                .agentEmetteur(emetteur)
                .agentValidateur(validateur)
                .ordreReparationId(b.getOrdreReparation() != null ? b.getOrdreReparation().getId() : null)
                .ordreReparation(ordreSummary)
                .ordreTravailId(b.getOrdreTravail() != null ? b.getOrdreTravail().getId() : null)
                .ordreTravail(ordreTravailSummary)
                .proformaId(b.getProforma() != null ? b.getProforma().getId() : null)
                .proformaNumero(b.getProforma() != null ? b.getProforma().getNumero() : null)
                .lignesBonDeSortiePieces(lignes)
                .build();
    }
}
