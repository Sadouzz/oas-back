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
    private BigDecimal montantTTC;
    private BigDecimal montantTimbre;
    private BigDecimal montantAutre;
    private BigDecimal montantTotal;
    private BigDecimal montantPaye;
    private BigDecimal resteAPayer;
    private StatutPaiement statutPaiement;

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
    private String numeroOrdreReparation;

    private List<LigneFacturationPieceResponse> lignesPieces;
    private List<LigneFacturationMainDoeuvreResponse> lignesMainDoeuvres;
    private List<RecuResponse> recus;

    public static FactureResponse from(sn.oas.facturation.features.facture.data.entity.Facture f) {
        if (f == null) return null;
        return FactureResponse.builder()
                .id(f.getId())
                .numero(f.getNumero())
                .dateCreation(f.getDateCreation())
                .dateModification(f.getDateModification())
                .montantHT(f.getMontantHT())
                .montantTVA(f.getMontantTVA())
                .montantTTC(f.getMontantTTC())
                .montantTimbre(f.getMontantTimbre())
                .montantAutre(f.getMontantAutre())
                .montantTotal(f.getMontantTotal())
                .montantPaye(f.getMontantPaye())
                .resteAPayer(f.getResteAPayer())
                .statutPaiement(f.getStatutPaiement())
                .agentId(f.getAgent() != null ? f.getAgent().getId() : null)
                .agentNom(f.getAgent() != null ? (f.getAgent().getFirstName() + " " + f.getAgent().getLastName()).trim() : null)
                .remarque(f.getRemarque())
                .kilometrage(f.getKilometrage())
                .clientId(f.getClient() != null ? f.getClient().getId() : null)
                .clientNom(f.getClient() != null ? (f.getClient().getFirstName() + " " + f.getClient().getLastName()).trim() : null)
                .vehiculeId(f.getVehicule() != null ? f.getVehicule().getId() : null)
                .immatriculation(f.getVehicule() != null ? f.getVehicule().getImmatriculation() : null)
                .numeroChassis(f.getVehicule() != null ? f.getVehicule().getNumeroChassis() : null)
                .marque(f.getVehicule() != null ? f.getVehicule().getMarque() : null)
                .modele(f.getVehicule() != null ? f.getVehicule().getModele() : null)
                .annee(f.getVehicule() != null ? f.getVehicule().getAnnee() : null)
                .numeroBonDeCommande(f.getNumeroBonDeCommande())
                .ordreReparationId(f.getOrdreReparation() != null ? f.getOrdreReparation().getId() : null)
                .numeroOrdreReparation(f.getOrdreReparation() != null ? f.getOrdreReparation().getNumero() : null)
                .lignesPieces(f.getLignesFacturationPieces() == null ? List.of() : f.getLignesFacturationPieces().stream()
                        .map(lp -> LigneFacturationPieceResponse.builder()
                                .id(lp.getId())
                                .pieceId(lp.getPiece() != null ? lp.getPiece().getId() : null)
                                .designationPiece(lp.getPiece() != null ? lp.getPiece().getDesignation() : null)
                                .quantite(lp.getQuantite())
                                .prix(lp.getPrix())
                                .montantTotal(lp.getQuantite() * lp.getPrix())
                                .build())
                        .toList())
                .lignesMainDoeuvres(f.getLignesFacturationMainDoeuvres() == null ? List.of() : f.getLignesFacturationMainDoeuvres().stream()
                        .map(lm -> LigneFacturationMainDoeuvreResponse.builder()
                                .id(lm.getId())
                                .mainDoeuvreId(lm.getMainDoeuvre() != null ? lm.getMainDoeuvre().getId() : null)
                                .descriptionMainDoeuvre(lm.getMainDoeuvre() != null && lm.getMainDoeuvre().getCategorie() != null ? lm.getMainDoeuvre().getCategorie().getNom() : null)
                                .nbreHeure(lm.getNbreHeure())
                                .tarifHoraire(lm.getTarifHoraire())
                                .montantTotal(lm.getNbreHeure() * lm.getTarifHoraire())
                                .build())
                        .toList())
                .recus(f.getRecus() == null ? List.of() : f.getRecus().stream()
                        .map(RecuResponse::from)
                        .toList())
                .build();
    }
}
