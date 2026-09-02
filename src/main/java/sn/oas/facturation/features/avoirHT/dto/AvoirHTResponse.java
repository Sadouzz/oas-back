package sn.oas.facturation.features.avoirHT.dto;

import lombok.Builder;
import lombok.Data;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreResponse;
import sn.oas.facturation.features.facturation.dto.LigneFacturationPieceResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AvoirHTResponse {
    private Long id;
    private String numero;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private BigDecimal montantHT;
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

    private List<LigneFacturationPieceResponse> lignesPieces;
    private List<LigneFacturationMainDoeuvreResponse> lignesMainDoeuvres;

    public static AvoirHTResponse from(sn.oas.facturation.features.avoirHT.data.entity.AvoirHT a) {
        if (a == null) return null;
        var client = a.getClient();
        if (client == null && a.getOrdreReparation() != null && a.getOrdreReparation().getVehicule() != null) {
            client = a.getOrdreReparation().getVehicule().getClient();
        }

        var vehicule = a.getVehicule();
        if (vehicule == null && a.getOrdreReparation() != null) {
            vehicule = a.getOrdreReparation().getVehicule();
        }

        return AvoirHTResponse.builder()
                .id(a.getId())
                .numero(a.getNumero())
                .dateCreation(a.getDateCreation())
                .dateModification(a.getDateModification())
                .montantHT(a.getMontantHT())
                .montantTotal(a.getMontantTotal() != null ? a.getMontantTotal() : a.getMontantHT())
                .agentId(a.getAgent() != null ? a.getAgent().getId() : null)
                .agentNom(a.getAgent() != null ? (a.getAgent().getFirstName() + " " + a.getAgent().getLastName()).trim() : null)
                .remarque(a.getRemarque())
                .kilometrage(a.getKilometrage())
                .clientId(client != null ? client.getId() : null)
                .clientNom(client != null ? (client.getFirstName() + " " + client.getLastName()).trim() : null)
                .vehiculeId(vehicule != null ? vehicule.getId() : null)
                .immatriculation(vehicule != null ? vehicule.getImmatriculation() : null)
                .numeroChassis(vehicule != null ? vehicule.getNumeroChassis() : null)
                .marque(vehicule != null ? vehicule.getMarque() : null)
                .modele(vehicule != null ? vehicule.getModele() : null)
                .annee(vehicule != null ? vehicule.getAnnee() : null)
                .numeroBonDeCommande(a.getBonDeCommande() != null ? a.getBonDeCommande().getNumero() : null)
                .lignesPieces(a.getLignesFacturationPieces() == null ? List.of()
                        : a.getLignesFacturationPieces().stream()
                                .map(lp -> LigneFacturationPieceResponse.builder()
                                        .id(lp.getId())
                                        .pieceId(lp.getPiece() != null ? lp.getPiece().getId() : null)
                                        .designationPiece(lp.getPiece() != null ? lp.getPiece().getDesignation()
                                                : lp.getDesignationPds())
                                        .quantite(lp.getQuantite())
                                        .prix(lp.getPrix())
                                        .montantTotal(lp.getQuantite() * lp.getPrix())
                                        .build())
                                .toList())
                .lignesMainDoeuvres(a.getLignesFacturationMainDoeuvres() == null ? List.of()
                        : a.getLignesFacturationMainDoeuvres().stream()
                                .map(lm -> LigneFacturationMainDoeuvreResponse.builder()
                                        .id(lm.getId())
                                        .mainDoeuvreId(lm.getMainDoeuvre() != null ? lm.getMainDoeuvre().getId() : null)
                                        .descriptionMainDoeuvre(lm.getMainDoeuvre() != null
                                                && lm.getMainDoeuvre().getCategorie() != null
                                                        ? lm.getMainDoeuvre().getCategorie().getNom()
                                                        : null)
                                        .nbreHeure(lm.getNbreHeure())
                                        .tarifHoraire(lm.getTarifHoraire())
                                        .montantTotal(lm.getNbreHeure() * lm.getTarifHoraire())
                                        .build())
                                .toList())
                .build();
    }
}
