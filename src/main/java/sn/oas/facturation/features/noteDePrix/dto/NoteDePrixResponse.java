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
    private BigDecimal montantTVA;
    private Boolean tvaApplicable;
    private BigDecimal montantTotal;
    private BigDecimal montantPaye;
    private BigDecimal montantTimbrePaye;
    private BigDecimal montantEncaisse;
    private BigDecimal resteAPayer;
    private String statutPaiement;
    private String modePaiement;
    private String numeroBonDeCommande;
    private BigDecimal montantAutre;

    private Long agentId;
    private String agentNom;
    private Long clientId;
    private String clientNom;
    private String clientMatricule;
    private String clientNumero;
    private String numeroClient;
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

        var client = note.getClient();
        if (client == null && note.getOrdreReparation() != null) {
            client = note.getOrdreReparation().getClient() != null
                    ? note.getOrdreReparation().getClient()
                    : (note.getOrdreReparation().getVehicule() != null ? note.getOrdreReparation().getVehicule().getClient() : null);
        }
        if (client == null && note.getVehicule() != null) {
            client = note.getVehicule().getClient();
        }

        Long clientId = null;
        String clientNom = null;
        String clientMatricule = null;
        if (client != null) {
            clientId = client.getId();
            String fn = client.getFirstName() != null ? client.getFirstName() : "";
            String ln = client.getLastName() != null ? client.getLastName() : "";
            clientNom = (fn + " " + ln).trim();
            clientMatricule = client.getMatricule();
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

        List<LigneFacturationPieceResponse> piecesResp = sn.oas.facturation.shared.OrdreAffichageLignes.plusRecentesEnPremier(
                note.getLignesFacturationPieces(), sn.oas.facturation.features.facturation.data.entity.LigneFacturationPiece::getId)
                .stream()
                        .map(lp -> {
                            int q = lp.getQuantite() != null ? lp.getQuantite() : 0;
                            int p = lp.getPrix() != null ? lp.getPrix() : 0;
                            double remise = lp.getTauxRemise() == null ? 0d : lp.getTauxRemise().doubleValue();
                            int total = (int) Math.round(q * p * (1 - remise / 100));

                            String designation = null;
                            if (lp.getPiece() != null && lp.getPiece().getDesignation() != null && !lp.getPiece().getDesignation().isBlank()) {
                                designation = lp.getPiece().getDesignation();
                            } else if (lp.getDesignationPds() != null && !lp.getDesignationPds().isBlank()) {
                                designation = lp.getDesignationPds();
                            } else {
                                designation = "Pièce";
                            }

                            String type;
                            if (lp.getPiece() != null && lp.getPiece().getType() != null) {
                                type = lp.getPiece().getType().name();
                            } else {
                                type = "PDS";
                            }

                            String reference = lp.getPiece() != null ? lp.getPiece().getReference() : null;
                            String depotNom = lp.getPiece() != null && lp.getPiece().getDepot() != null ? lp.getPiece().getDepot().getNom() : null;
                            boolean isCustom = lp.getIsCustom() != null ? lp.getIsCustom() : (lp.getPiece() == null);

                            return LigneFacturationPieceResponse.builder()
                                    .id(lp.getId())
                                    .dateHeureSaisie(lp.getDateHeureSaisie())
                                    .pieceId(lp.getPiece() != null ? lp.getPiece().getId() : null)
                                    .type(type)
                                    .designationPiece(designation)
                                    .isCustom(isCustom)
                                    .designationPds(lp.getDesignationPds() != null ? lp.getDesignationPds() : designation)
                                    .referencePiece(reference)
                                    .depotNom(depotNom)
                                    .quantite(q)
                                    .prix(p)
                                    .montantTotal(total)
                                    .tauxRemise(remise)
                                    .datesHeuresSaisieSource(lp.getDatesHeuresSaisieSource() == null ? List.of() : List.copyOf(lp.getDatesHeuresSaisieSource()))
                                    .build();
                        })
                        .toList();

        List<LigneFacturationMainDoeuvreResponse> moResp = (note.getLignesFacturationMainDoeuvres() == null) ? List.of()
                : note.getLignesFacturationMainDoeuvres().stream()
                        .map(lm -> {
                            int h = lm.getNbreHeure() != null ? lm.getNbreHeure() : 0;
                            int t = lm.getTarifHoraire() != null ? lm.getTarifHoraire() : 0;
                            double remiseMo = lm.getTauxRemise() != null ? lm.getTauxRemise().doubleValue() : 0.0;
                            int totalMo = (int) Math.round(h * t * (1 - remiseMo / 100));

                            String descriptionMo = "Main d'œuvre";
                            if (lm.getMainDoeuvre() != null) {
                                if (lm.getMainDoeuvre().getDescription() != null && !lm.getMainDoeuvre().getDescription().isBlank()) {
                                    descriptionMo = lm.getMainDoeuvre().getDescription();
                                } else if (lm.getMainDoeuvre().getCategorie() != null && lm.getMainDoeuvre().getCategorie().getNom() != null) {
                                    descriptionMo = lm.getMainDoeuvre().getCategorie().getNom();
                                }
                            }

                            return LigneFacturationMainDoeuvreResponse.builder()
                                    .id(lm.getId())
                                    .dateHeureSaisie(lm.getDateHeureSaisie())
                                    .mainDoeuvreId(lm.getMainDoeuvre() != null ? lm.getMainDoeuvre().getId() : null)
                                    .descriptionMainDoeuvre(descriptionMo)
                                    .nbreHeure(h)
                                    .tarifHoraire(t)
                                    .montantTotal(totalMo)
                                    .tauxRemise(remiseMo)
                                    .build();
                        })
                        .toList();

        return NoteDePrixResponse.builder()
                .id(note.getId())
                .numero(note.getNumero())
                .dateCreation(note.getDateCreation())
                .montantHT(note.getMontantHT() != null ? note.getMontantHT() : BigDecimal.ZERO)
                .montantTVA(note.getMontantTVA() != null ? note.getMontantTVA() : BigDecimal.ZERO)
                .tvaApplicable(note.getTvaApplicable())
                .montantTotal(note.getMontantTotal() != null ? note.getMontantTotal() : BigDecimal.ZERO)
                .montantPaye(note.getMontantPaye() != null ? note.getMontantPaye() : BigDecimal.ZERO)
                .montantTimbrePaye(note.getMontantTimbrePaye() != null ? note.getMontantTimbrePaye() : BigDecimal.ZERO)
                .montantEncaisse((note.getMontantPaye() != null ? note.getMontantPaye() : BigDecimal.ZERO)
                        .add(note.getMontantTimbrePaye() != null ? note.getMontantTimbrePaye() : BigDecimal.ZERO))
                .resteAPayer(note.getResteAPayer() != null ? note.getResteAPayer() : (note.getMontantTotal() != null ? note.getMontantTotal() : BigDecimal.ZERO))
                .statutPaiement(note.getStatutPaiement() != null ? note.getStatutPaiement().name() : "NON_PAYE")
                .modePaiement(note.getModePaiement())
                .numeroBonDeCommande(note.getNumeroBonDeCommande())
                .montantAutre(note.getMontantAutre() != null ? note.getMontantAutre() : BigDecimal.ZERO)
                .agentId(agentId)
                .agentNom(agentNom)
                .clientId(clientId)
                .clientNom(clientNom)
                .clientMatricule(clientMatricule)
                .clientNumero(clientMatricule)
                .numeroClient(clientMatricule)
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
