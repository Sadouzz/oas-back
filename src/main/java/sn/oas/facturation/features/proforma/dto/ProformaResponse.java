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

    private String statut;
    private Boolean visibleClient;

    private List<LigneFacturationPieceResponse> lignesPieces;
    private List<LigneFacturationMainDoeuvreResponse> lignesMainDoeuvres;

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

        if (p.getOrdreReparation() != null && p.getOrdreReparation().getVehicule() != null) {
            var vehicule = p.getOrdreReparation().getVehicule();
            vehiculeId = vehicule.getId();
            immatriculation = vehicule.getImmatriculation();
            numeroChassis = vehicule.getNumeroChassis();
            marque = vehicule.getMarque();
            modele = vehicule.getModele();
            annee = vehicule.getAnnee();
            if (vehicule.getClient() != null) {
                clientId = vehicule.getClient().getId();
                clientNom = (vehicule.getClient().getFirstName() + " " + vehicule.getClient().getLastName()).trim();
            }
        }

        return ProformaResponse.builder()
                .id(p.getId())
                .numero(p.getNumero())
                .dateCreation(p.getDateCreation())
                .dateModification(p.getDateModification())
                .montantHT(p.getMontantHT())
                .montantTVA(p.getMontantTVA())
                .montantTTC(p.getMontantTTC())
                .montantTimbre(p.getMontantTimbre())
                .montantAutre(BigDecimal.ZERO)
                .montantTotal(p.getMontantTotal())
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
                .lignesPieces(p.getLignesFacturationPieces() == null ? List.of() : p.getLignesFacturationPieces().stream()
                        .map(lp -> LigneFacturationPieceResponse.builder()
                                .id(lp.getId())
                                .pieceId(lp.getPiece() != null ? lp.getPiece().getId() : null)
                                .designationPiece(lp.getPiece() != null ? lp.getPiece().getDesignation() : null)
                                .quantite(lp.getQuantite())
                                .prix(lp.getPrix())
                                .montantTotal(lp.getQuantite() * lp.getPrix())
                                .build())
                        .toList())
                .lignesMainDoeuvres(p.getLignesFacturationMainDoeuvres() == null ? List.of() : p.getLignesFacturationMainDoeuvres().stream()
                        .map(lm -> LigneFacturationMainDoeuvreResponse.builder()
                                .id(lm.getId())
                                .mainDoeuvreId(lm.getMainDoeuvre() != null ? lm.getMainDoeuvre().getId() : null)
                                .descriptionMainDoeuvre(lm.getMainDoeuvre() != null && lm.getMainDoeuvre().getCategorie() != null ? lm.getMainDoeuvre().getCategorie().getNom() : null)
                                .nbreHeure(lm.getNbreHeure())
                                .tarifHoraire(lm.getTarifHoraire())
                                .montantTotal(lm.getNbreHeure() * lm.getTarifHoraire())
                                .build())
                        .toList())
                .build();
    }
}
