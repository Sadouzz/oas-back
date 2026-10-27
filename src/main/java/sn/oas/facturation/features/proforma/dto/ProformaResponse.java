package sn.oas.facturation.features.proforma.dto;

import lombok.Builder;
import lombok.Data;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreResponse;
import sn.oas.facturation.features.facturation.dto.LigneFacturationPieceResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProformaResponse {
    private Long id;
    private String numero;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private Boolean tvaApplicable;
    private BigDecimal montantTTC;
    private BigDecimal montantTimbre;
    private BigDecimal montantAutre;
    private BigDecimal montantTotal;
    private Long agentId;
    private String agentNom;
    private String remarque;
    private Double kilometrage;

    private Long clientId;
    private String clientNom;
    
    private Long vehiculeId;
    private String immatriculation;
    private String numeroChassis;
    private String marque;
    private String modele;
    private Integer annee;

    private String numeroBonDeCommande;
    private Long ordreReparationId;
    private Long bsId;
    private String bsReference;

    private String statut;
    private Boolean visibleClient;

    private List<LigneFacturationPieceResponse> lignesPieces;
    private List<LigneFacturationMainDoeuvreResponse> lignesMainDoeuvres;
    private BigDecimal tauxRemiseClient;
    private BigDecimal montantRemiseClient;
    private List<String> avertissementsFinanciers;

    public static ProformaResponse from(sn.oas.facturation.features.proforma.data.entity.Proforma p) {
        if (p == null) return null;
        Long clientId = null;
        String clientNom = null;
        Long vehiculeId = null;
        String immatriculation = null;
        String numeroChassis = null;
        String marque = null;
        String modele = null;
        Integer annee = null;

        var vehicule = p.getVehicule();
        if (vehicule == null && p.getOrdreReparation() != null) vehicule = p.getOrdreReparation().getVehicule();
        if (vehicule != null) {
            vehiculeId = vehicule.getId();
            immatriculation = vehicule.getImmatriculation();
            numeroChassis = vehicule.getNumeroChassis();
            marque = vehicule.getMarque();
            modele = vehicule.getModele();
            annee = vehicule.getAnnee();
            var client = p.getClient();
            if (client == null && p.getOrdreReparation() != null) client = p.getOrdreReparation().getClient();
            if (client == null) client = vehicule.getClient();
            if (client != null) {
                clientId = client.getId();
                clientNom = (client.getFirstName() + " " + client.getLastName()).trim();
            }
        }

        return ProformaResponse.builder()
                .id(p.getId())
                .numero(p.getNumero())
                .dateCreation(p.getDateCreation())
                .dateModification(p.getDateModification())
                .montantHT(p.getMontantHT())
                .montantTVA(p.getMontantTVA())
                .tvaApplicable(p.getTvaApplicable())
                .montantTTC(p.calculerMontantTtcAvecTimbre())
                .montantTimbre(p.getMontantTimbre())
                .montantAutre(BigDecimal.ZERO)
                .montantTotal(p.getMontantTotal())
                .tauxRemiseClient(p.getTauxRemiseClient())
                .montantRemiseClient(p.getMontantRemiseClient())
                .avertissementsFinanciers(p.getAvertissementsFinanciers() == null ? List.of() : p.getAvertissementsFinanciers())
                .statut(p.getStatut() != null ? p.getStatut().name() : null)
                .visibleClient(p.getVisibleClient() != null ? p.getVisibleClient() : Boolean.FALSE)
                .agentId(p.getAgent() != null ? p.getAgent().getId() : null)
                .agentNom(p.getAgent() != null ? (p.getAgent().getFirstName() + " " + p.getAgent().getLastName()).trim() : null)
                .remarque(p.getRemarque())
                .kilometrage(p.getKilometrage())
                .clientId(clientId)
                .clientNom(clientNom)
                .vehiculeId(vehiculeId)
                .immatriculation(immatriculation)
                .numeroChassis(numeroChassis)
                .marque(marque)
                .modele(modele)
                .annee(annee)
                .numeroBonDeCommande(p.getBonDeCommande() != null ? p.getBonDeCommande().getNumero() : null)
                .ordreReparationId(p.getOrdreReparation() != null ? p.getOrdreReparation().getId() : null)
                .bsId(p.getBs() != null ? p.getBs().getId() : null)
                .bsReference(p.getBs() != null ? p.getBs().getReference() : null)
                .lignesPieces(p.getLignesFacturationPieces() == null ? List.of() : p.getLignesFacturationPieces().stream()
                        .map(lp -> {
                            Long pieceId = lp.getPiece() != null ? lp.getPiece().getId() : null;
                            String type = Boolean.TRUE.equals(lp.getIsCustom()) ? "PDS" :
                                    (lp.getPiece() != null && lp.getPiece().getType() != null ? lp.getPiece().getType().name() : "PDP");
                            String ref = lp.getPiece() != null ? lp.getPiece().getReference() : null;
                            String designation = lp.getPiece() != null ? lp.getPiece().getDesignation() : lp.getDesignationPds();
                            int qte = lp.getQuantite() != null ? lp.getQuantite() : 1;
                            int prix = lp.getPrix() != null ? lp.getPrix() : 0;
                            double tauxRemise = lp.getTauxRemise() != null ? lp.getTauxRemise().doubleValue() : 0.0;
                            int montantLigne = (int) Math.round(qte * prix * (1 - tauxRemise / 100));

                            return LigneFacturationPieceResponse.builder()
                                    .id(lp.getId())
                                    .pieceId(pieceId)
                                    .type(type)
                                    .referencePiece(ref)
                                    .designationPiece(designation)
                                    .designationPds(lp.getDesignationPds())
                                    .isCustom(lp.getIsCustom() != null ? lp.getIsCustom() : (pieceId == null))
                                    .quantite(qte)
                                    .prix(prix)
                                    .montantTotal(montantLigne)
                                    .tauxRemise(tauxRemise)
                                    .build();
                        })
                        .toList())
                .lignesMainDoeuvres(p.getLignesFacturationMainDoeuvres() == null ? List.of() : p.getLignesFacturationMainDoeuvres().stream()
                        .map(lm -> {
                            Long moId = lm.getMainDoeuvre() != null ? lm.getMainDoeuvre().getId() : null;
                            String desc = lm.getMainDoeuvre() != null ?
                                    (lm.getMainDoeuvre().getCategorie() != null ? lm.getMainDoeuvre().getCategorie().getNom() : lm.getMainDoeuvre().getDescription()) : null;
                            int heures = lm.getNbreHeure() != null ? lm.getNbreHeure() : 0;
                            int tarif = lm.getTarifHoraire() != null ? lm.getTarifHoraire() : 0;
                            double tauxRemise = lm.getTauxRemise() != null ? lm.getTauxRemise().doubleValue() : 0.0;
                            int montantLigne = (int) Math.round(heures * tarif * (1 - tauxRemise / 100));

                            return LigneFacturationMainDoeuvreResponse.builder()
                                    .id(lm.getId())
                                    .mainDoeuvreId(moId)
                                    .descriptionMainDoeuvre(desc)
                                    .nbreHeure(heures)
                                    .tarifHoraire(tarif)
                                    .montantTotal(montantLigne)
                                    .tauxRemise(tauxRemise)
                                    .build();
                        })
                        .toList())
                .build();
    }
}
