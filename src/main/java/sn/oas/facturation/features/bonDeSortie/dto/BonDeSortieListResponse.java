package sn.oas.facturation.features.bonDeSortie.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.bonDeSortie.data.enums.StatutBon;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        String ordreReparationStatut,
        Long ordreTravailId,
        OrdreTravailSummary ordreTravail,
        String ordreTravailStatut,
        Double montantTotalPieces,
        Double montantTotalMainDoeuvre,
        Double montantTotal,
        Long proformaId,
        String proformaNumero,
        @JsonAlias({"lignesBonDeSortiePieces", "pieces"})
        List<LigneSummary> lignesPieces,
        @JsonAlias({"lignesBonDeSortieMainDoeuvres", "mainDoeuvres", "lignesMO"})
        List<LigneMainDoeuvreSummary> lignesMainDoeuvres
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
            String type,
            String designation,
            Integer quantite,
            Integer prix,
            Double tauxRemise,
            Double montantTotal
    ) {}
    public record LigneMainDoeuvreSummary(
            Long id,
            Long mainDoeuvreId,
            String code,
            String designation,
            Integer nbreHeure,
            Integer tarifHoraire,
            Double tauxRemise,
            Double montantTotal
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

        List<LigneSummary> lignesPieces = new ArrayList<>();
        List<LigneMainDoeuvreSummary> lignesMo = new ArrayList<>();

        if (b.getLignesBonDeSortiePieces() != null) {
            for (var l : b.getLignesBonDeSortiePieces()) {
                double tauxRemise = l.getTauxRemise() != null ? l.getTauxRemise().doubleValue() : 0.0;
                int qte = l.getQuantite() != null ? l.getQuantite() : 1;

                PieceSummary pSummary = null;
                if (l.getPiece() != null) {
                    pSummary = new PieceSummary(
                            l.getPiece().getId(),
                            l.getPiece().getReference(),
                            l.getPiece().getDesignation(),
                            l.getPiece().getPrixUnitaire()
                    );
                }
                int prix = l.getPrix() != null ? l.getPrix()
                        : (l.getPiece() != null && l.getPiece().getPrixUnitaire() != null ? l.getPiece().getPrixUnitaire().intValue() : 0);
                double montant = l.getMontantTotal() != null ? l.getMontantTotal().doubleValue()
                        : (double) (qte * prix * (1.0 - tauxRemise / 100.0));

                String pieceType = l.getPiece() != null && l.getPiece().getType() != null ? l.getPiece().getType().name()
                        : (l.getPiece() instanceof sn.oas.facturation.features.piecedetache.data.entity.PDP ? "PDP"
                        : (l.getPiece() instanceof sn.oas.facturation.features.piecedetache.data.entity.PDG ? "PDG" : "PDS"));

                lignesPieces.add(new LigneSummary(
                        l.getId(),
                        pSummary,
                        pieceType,
                        l.getDesignation() != null ? l.getDesignation() : (l.getPiece() != null ? l.getPiece().getDesignation() : ""),
                        qte,
                        prix,
                        tauxRemise,
                        montant
                ));
            }
        }

        if (b.getLignesBonDeSortieMainDoeuvres() != null) {
            for (var lmo : b.getLignesBonDeSortieMainDoeuvres()) {
                String code = lmo.getMainDoeuvre() != null && lmo.getMainDoeuvre().getId() != null
                        ? "MO-" + lmo.getMainDoeuvre().getId() : null;
                String designation = lmo.getDescription() != null ? lmo.getDescription()
                        : (lmo.getMainDoeuvre() != null ? lmo.getMainDoeuvre().getDescription() : "Main d'œuvre");
                double tauxRemise = lmo.getTauxRemise() != null ? lmo.getTauxRemise().doubleValue() : 0.0;
                int tarif = lmo.getTarifHoraire() != null ? lmo.getTarifHoraire() : 0;
                int heures = lmo.getNbreHeure() != null ? lmo.getNbreHeure() : 1;
                double montant = lmo.getMontantTotal() != null ? lmo.getMontantTotal().doubleValue()
                        : (double) (heures * tarif * (1.0 - tauxRemise / 100.0));

                lignesMo.add(new LigneMainDoeuvreSummary(
                        lmo.getId(),
                        lmo.getMainDoeuvre() != null ? lmo.getMainDoeuvre().getId() : null,
                        code,
                        designation,
                        heures,
                        tarif,
                        tauxRemise,
                        montant
                ));
            }
        }

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
                .ordreReparationStatut(b.getOrdreReparation() != null && b.getOrdreReparation().getStatut() != null ? b.getOrdreReparation().getStatut().name() : null)
                .ordreTravailId(b.getOrdreTravail() != null ? b.getOrdreTravail().getId() : null)
                .ordreTravail(ordreTravailSummary)
                .ordreTravailStatut(b.getOrdreTravail() != null && b.getOrdreTravail().getStatut() != null ? b.getOrdreTravail().getStatut().name() : null)
                .montantTotalPieces(b.getMontantTotalPieces() != null ? b.getMontantTotalPieces().doubleValue() : 0.0)
                .montantTotalMainDoeuvre(b.getMontantTotalMainDoeuvre() != null ? b.getMontantTotalMainDoeuvre().doubleValue() : 0.0)
                .montantTotal(b.getMontantTotal() != null ? b.getMontantTotal().doubleValue() : 0.0)
                .proformaId(b.getProforma() != null ? b.getProforma().getId() : null)
                .proformaNumero(b.getProforma() != null ? b.getProforma().getNumero() : null)
                .lignesPieces(lignesPieces)
                .lignesMainDoeuvres(lignesMo)
                .build();
    }
}
