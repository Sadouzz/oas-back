package sn.oas.facturation.features.noteDePrix.dto;

import lombok.Builder;
import lombok.Data;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreResponse;
import sn.oas.facturation.features.facturation.dto.LigneFacturationPieceResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class NoteDePrixResponse {
    private Long id;
    private String numero;
    private LocalDateTime dateCreation;
    private BigDecimal montantHT;
    private BigDecimal montantTotal;
    private BigDecimal montantPaye;
    private BigDecimal resteAPayer;
    private String statutPaiement;
    private String modePaiement;
    private String numeroBonDeCommande;
    private BigDecimal montantAutre;

    private Long agentId;
    private String agentNom;
    private Long clientId;
    private String clientNom;
    private Long vehiculeId;
    private String vehiculeImmatriculation;
    private String immatriculation;
    private String marque;
    private String modele;
    private String numeroChassis;
    private Double kilometrage;
    private String remarque;
    private String statut;
    private Long ordreReparationId;
    private String numeroOrdreReparation;
    
    private List<LigneFacturationPieceResponse> lignesPieces;
    private List<LigneFacturationMainDoeuvreResponse> lignesMainDoeuvres;

    public static NoteDePrixResponse from(sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix note) {
        if (note == null) return null;

        Long agentId = null;
        String agentNom = null;
        if (note.getAgent() != null) {
            agentId = note.getAgent().getId();
            String fn = note.getAgent().getFirstName() != null ? note.getAgent().getFirstName() : "";
            String ln = note.getAgent().getLastName() != null ? note.getAgent().getLastName() : "";
            agentNom = (fn + " " + ln).trim();
        }

        Long clientId = null;
        String clientNom = null;
        if (note.getClient() != null) {
            clientId = note.getClient().getId();
            String fn = note.getClient().getFirstName() != null ? note.getClient().getFirstName() : "";
            String ln = note.getClient().getLastName() != null ? note.getClient().getLastName() : "";
            clientNom = (fn + " " + ln).trim();
        } else if (note.getOrdreReparation() != null && note.getOrdreReparation().getVehicule() != null
                && note.getOrdreReparation().getVehicule().getClient() != null) {
            clientId = note.getOrdreReparation().getVehicule().getClient().getId();
            String fn = note.getOrdreReparation().getVehicule().getClient().getFirstName() != null ? note.getOrdreReparation().getVehicule().getClient().getFirstName() : "";
            String ln = note.getOrdreReparation().getVehicule().getClient().getLastName() != null ? note.getOrdreReparation().getVehicule().getClient().getLastName() : "";
            clientNom = (fn + " " + ln).trim();
        }

        Long vehiculeId = null;
        String vehiculeImmat = null;
        String marque = null;
        String modele = null;
        String numeroChassis = null;
        if (note.getVehicule() != null) {
            vehiculeId = note.getVehicule().getId();
            vehiculeImmat = note.getVehicule().getImmatriculation();
            marque = note.getVehicule().getMarque();
            modele = note.getVehicule().getModele();
            numeroChassis = note.getVehicule().getNumeroChassis();
        } else if (note.getOrdreReparation() != null && note.getOrdreReparation().getVehicule() != null) {
            vehiculeId = note.getOrdreReparation().getVehicule().getId();
            vehiculeImmat = note.getOrdreReparation().getVehicule().getImmatriculation();
            marque = note.getOrdreReparation().getVehicule().getMarque();
            modele = note.getOrdreReparation().getVehicule().getModele();
            numeroChassis = note.getOrdreReparation().getVehicule().getNumeroChassis();
        }

        Long ordreReparationId = note.getOrdreReparation() != null ? note.getOrdreReparation().getId() : null;
        String numeroOrdreReparation = note.getOrdreReparation() != null ? note.getOrdreReparation().getNumero() : null;

        List<LigneFacturationPieceResponse> piecesResp = (note.getLignesFacturationPieces() == null) ? List.of()
                : note.getLignesFacturationPieces().stream()
                        .map(lp -> {
                            int q = lp.getQuantite() != null ? lp.getQuantite() : 0;
                            int p = lp.getPrix() != null ? lp.getPrix() : 0;
                            return LigneFacturationPieceResponse.builder()
                                    .id(lp.getId())
                                    .pieceId(lp.getPiece() != null ? lp.getPiece().getId() : null)
                                    .designationPiece(
                                            lp.getPiece() != null ? lp.getPiece().getDesignation() : "Pièce")
                                    .quantite(q)
                                    .prix(p)
                                    .montantTotal(p * q)
                                    .build();
                        })
                        .toList();

        List<LigneFacturationMainDoeuvreResponse> moResp = (note.getLignesFacturationMainDoeuvres() == null) ? List.of()
                : note.getLignesFacturationMainDoeuvres().stream()
                        .map(lm -> {
                            int h = lm.getNbreHeure() != null ? lm.getNbreHeure() : 0;
                            int t = lm.getTarifHoraire() != null ? lm.getTarifHoraire() : 0;
                            return LigneFacturationMainDoeuvreResponse.builder()
                                    .id(lm.getId())
                                    .mainDoeuvreId(lm.getMainDoeuvre() != null ? lm.getMainDoeuvre().getId() : null)
                                    .descriptionMainDoeuvre(
                                            lm.getMainDoeuvre() != null && lm.getMainDoeuvre().getCategorie() != null
                                                    ? lm.getMainDoeuvre().getCategorie().getNom()
                                                    : "Main d'œuvre")
                                    .nbreHeure(h)
                                    .tarifHoraire(t)
                                    .montantTotal(h * t)
                                    .build();
                        })
                        .toList();

        return NoteDePrixResponse.builder()
                .id(note.getId())
                .numero(note.getNumero())
                .dateCreation(note.getDateCreation())
                .montantHT(note.getMontantHT() != null ? note.getMontantHT() : BigDecimal.ZERO)
                .montantTotal(note.getMontantTotal() != null ? note.getMontantTotal() : BigDecimal.ZERO)
                .montantPaye(note.getMontantPaye() != null ? note.getMontantPaye() : BigDecimal.ZERO)
                .resteAPayer(note.getResteAPayer() != null ? note.getResteAPayer() : (note.getMontantTotal() != null ? note.getMontantTotal() : BigDecimal.ZERO))
                .statutPaiement(note.getStatutPaiement() != null ? note.getStatutPaiement().name() : "NON_PAYE")
                .modePaiement(note.getModePaiement())
                .numeroBonDeCommande(note.getNumeroBonDeCommande())
                .montantAutre(note.getMontantAutre() != null ? note.getMontantAutre() : BigDecimal.ZERO)
                .agentId(agentId)
                .agentNom(agentNom)
                .clientId(clientId)
                .clientNom(clientNom)
                .vehiculeId(vehiculeId)
                .vehiculeImmatriculation(vehiculeImmat)
                .immatriculation(vehiculeImmat)
                .marque(marque)
                .modele(modele)
                .numeroChassis(numeroChassis)
                .kilometrage(note.getKilometrage())
                .remarque(note.getRemarque())
                .statut(note.getStatut() != null ? note.getStatut().name() : null)
                .ordreReparationId(ordreReparationId)
                .numeroOrdreReparation(numeroOrdreReparation)
                .lignesPieces(piecesResp)
                .lignesMainDoeuvres(moResp)
                .build();
    }
}
