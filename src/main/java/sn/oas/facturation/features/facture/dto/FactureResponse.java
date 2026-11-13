package sn.oas.facturation.features.facture.dto;

import lombok.Builder;
import lombok.Data;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreResponse;
import sn.oas.facturation.features.facturation.dto.LigneFacturationPieceResponse;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;
import sn.oas.facturation.features.recu.dto.RecuResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class FactureResponse {
    private Long id;
    private String numero;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private Boolean tvaApplicable;
    private BigDecimal montantTTC;
    private BigDecimal montantTimbre;
    private BigDecimal timbreFiscalRate;
    private String modePaiement;
    private BigDecimal montantAutre;
    private BigDecimal montantTotal;
    private BigDecimal tauxRemiseClient;
    private BigDecimal montantRemiseClient;
    private LocalDateTime dateEcheance;
    private BigDecimal montantPaye;
    private BigDecimal resteAPayer;
    private StatutPaiement statutPaiement;

    private Long agentId;
    private String agentNom;
    private String remarque;
    private Double kilometrage;

    private Long clientId;
    private String clientNom;
    private String clientMatricule;
    private String clientNumero;
    private String numeroClient;
    
    private Long vehiculeId;
    private String vehiculeImmatriculation;
    private String immatriculation;
    private String numeroChassis;
    private String marque;
    private String modele;
    private Integer annee;

    private String numeroBonDeCommande;

    private Long ordreReparationId;
    private String numeroOrdreReparation;

    private List<LigneFacturationPieceResponse> lignesPieces;
    private List<LigneFacturationMainDoeuvreResponse> lignesMainDoeuvres;
    private List<RecuResponse> recus;

    public static FactureResponse from(sn.oas.facturation.features.facture.data.entity.Facture f) {
        if (f == null) return null;
        var ordre = f.getOrdreReparation();
        var vehicule = f.getVehicule() != null ? f.getVehicule() : ordre != null ? ordre.getVehicule() : null;
        var client = f.getClient() != null ? f.getClient()
                : ordre != null && ordre.getClient() != null ? ordre.getClient()
                : vehicule != null ? vehicule.getClient() : null;
        String clientMatricule = client != null ? client.getMatricule() : null;
        String immat = vehicule != null ? vehicule.getImmatriculation() : null;
        return FactureResponse.builder()
                .id(f.getId())
                .numero(f.getNumero())
                .dateCreation(f.getDateCreation())
                .dateModification(f.getDateModification())
                .montantHT(f.getMontantHT())
                .montantTVA(f.getMontantTVA())
                .tvaApplicable(f.getTvaApplicable())
                .montantTTC(f.getMontantTTC())
                .montantTimbre(f.getMontantTimbre())
                .timbreFiscalRate(f.getTimbreFiscalRate())
                .modePaiement(f.getModePaiement())
                .montantAutre(f.getMontantAutre())
                .montantTotal(f.getMontantTotal())
                .tauxRemiseClient(f.getTauxRemiseClient())
                .montantRemiseClient(f.getMontantRemiseClient())
                .dateEcheance(f.getDateEcheance())
                .montantPaye(f.getMontantPaye())
                .resteAPayer(f.getResteAPayer())
                .statutPaiement(f.getStatutPaiement())
                .agentId(f.getAgent() != null ? f.getAgent().getId() : null)
                .agentNom(f.getAgent() != null ? (f.getAgent().getFirstName() + " " + f.getAgent().getLastName()).trim() : null)
                .remarque(f.getRemarque())
                .kilometrage(f.getKilometrage())
                .clientId(client != null ? client.getId() : null)
                .clientNom(client != null ? (client.getFirstName() + " " + client.getLastName()).trim() : null)
                .clientMatricule(clientMatricule)
                .clientNumero(clientMatricule)
                .numeroClient(clientMatricule)
                .vehiculeId(vehicule != null ? vehicule.getId() : null)
                .vehiculeImmatriculation(immat)
                .immatriculation(immat)
                .numeroChassis(vehicule != null ? vehicule.getNumeroChassis() : null)
                .marque(vehicule != null ? vehicule.getMarque() : null)
                .modele(vehicule != null ? vehicule.getModele() : null)
                .annee(vehicule != null ? vehicule.getAnnee() : null)
                .numeroBonDeCommande(f.getNumeroBonDeCommande())
                .ordreReparationId(f.getOrdreReparation() != null ? f.getOrdreReparation().getId() : null)
                .numeroOrdreReparation(f.getOrdreReparation() != null ? f.getOrdreReparation().getNumero() : null)
                .lignesPieces(sn.oas.facturation.shared.OrdreAffichageLignes.plusRecentesEnPremier(
                        f.getLignesFacturationPieces(), sn.oas.facturation.features.facturation.data.entity.LigneFacturationPiece::getId).stream()
                        .map(lp -> {
                            int q = lp.getQuantite() != null ? lp.getQuantite() : 0;
                            int p = lp.getPrix() != null ? lp.getPrix() : 0;
                            double remise = lp.getTauxRemise() != null ? lp.getTauxRemise().doubleValue() : 0.0;
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
                        .toList())
                .lignesMainDoeuvres(f.getLignesFacturationMainDoeuvres() == null ? List.of() : f.getLignesFacturationMainDoeuvres().stream()
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
                        .toList())
                .recus(f.getRecus() == null ? List.of() : f.getRecus().stream()
                        .map(RecuResponse::from)
                        .toList())
                .build();
    }
}
