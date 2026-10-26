package sn.oas.facturation.features.bonDeReception.dto;

import lombok.Builder;
import lombok.Data;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreResponse;
import sn.oas.facturation.features.facturation.dto.LigneFacturationPieceResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BonDeReceptionResponse {
    private Long id;
    private String numero;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private Boolean tvaApplicable;
    private BigDecimal montantTTC;
    private BigDecimal montantTimbre;
    private BigDecimal montantTotal;
    private Long agentId;
    private String agentNom;
    private String remarque;
    private Double kilometrage;
    
    private Long bonDeCommandeId;
    private String bonDeCommandeNumero;
    private String fournisseurNom;
    private String referenceFactureFournisseur;
    private String statut;
    
    private List<LigneFacturationPieceResponse> lignesPieces;
    private List<LigneFacturationMainDoeuvreResponse> lignesMainDoeuvres;

    public static BonDeReceptionResponse from(sn.oas.facturation.features.bonDeReception.data.entity.BonDeReception bl) {
        if (bl == null) return null;
        return BonDeReceptionResponse.builder()
                .id(bl.getId())
                .numero(bl.getNumero())
                .dateCreation(bl.getDateCreation())
                .dateModification(bl.getDateModification())
                .montantHT(bl.getMontantHT())
                .montantTVA(bl.getMontantTVA())
                .tvaApplicable(bl.getTvaApplicable())
                .montantTTC(bl.getMontantTTC())
                .montantTimbre(bl.getMontantTimbre())
                .montantTotal(bl.getMontantTotal())
                .agentId(bl.getAgent() != null ? bl.getAgent().getId() : null)
                .agentNom(bl.getAgent() != null ? (bl.getAgent().getFirstName() + " " + bl.getAgent().getLastName()).trim() : null)
                .remarque(bl.getRemarque())
                .kilometrage(bl.getKilometrage())
                .bonDeCommandeId(bl.getBonDeCommande() != null ? bl.getBonDeCommande().getId() : null)
                .bonDeCommandeNumero(bl.getBonDeCommande() != null ? bl.getBonDeCommande().getNumero() : null)
                .fournisseurNom(bl.getBonDeCommande() != null && bl.getBonDeCommande().getFournisseur() != null
                        ? (bl.getBonDeCommande().getFournisseur().getNomEntreprise() != null
                            ? bl.getBonDeCommande().getFournisseur().getNomEntreprise()
                            : (bl.getBonDeCommande().getFournisseur().getPrenom() + " " + bl.getBonDeCommande().getFournisseur().getNom()).trim()) : null)
                .referenceFactureFournisseur(bl.getReferenceFactureFournisseur())
                .statut(bl.getBonDeCommande() == null || bl.getBonDeCommande().getStatut() == null ? "RECU"
                        : bl.getBonDeCommande().getStatut().name())
                .lignesPieces(mapLignesPieces(bl))
                .lignesMainDoeuvres(bl.getLignesFacturationMainDoeuvres() == null ? List.of() : bl.getLignesFacturationMainDoeuvres().stream()
                        .map(ligne -> LigneFacturationMainDoeuvreResponse.builder()
                                .id(ligne.getId())
                                .mainDoeuvreId(ligne.getMainDoeuvre() != null ? ligne.getMainDoeuvre().getId() : null)
                                .descriptionMainDoeuvre(ligne.getMainDoeuvre() != null && ligne.getMainDoeuvre().getCategorie() != null ? ligne.getMainDoeuvre().getCategorie().getNom() : null)
                                .nbreHeure(ligne.getNbreHeure())
                                .tarifHoraire(ligne.getTarifHoraire())
                                .montantTotal(ligne.getNbreHeure() * ligne.getTarifHoraire())
                                .build())
                        .toList())
                .build();
    }

    private static List<LigneFacturationPieceResponse> mapLignesPieces(
            sn.oas.facturation.features.bonDeReception.data.entity.BonDeReception bon) {
        var commande = bon.getBonDeCommande();
        var lignesReception = bon.getLignesFacturationPieces() == null ? List.<sn.oas.facturation.features.facturation.data.entity.LigneFacturationPiece>of()
                : bon.getLignesFacturationPieces();
        if (commande == null || commande.getLignes() == null || commande.getLignes().isEmpty()) {
            return lignesReception.stream().map(ligne -> LigneFacturationPieceResponse.builder()
                    .id(ligne.getId())
                    .pieceId(ligne.getPiece() != null ? ligne.getPiece().getId() : null)
                    .designationPiece(ligne.getPiece() != null ? ligne.getPiece().getDesignation() : ligne.getDesignationPds())
                    .referencePiece(ligne.getPiece() != null ? ligne.getPiece().getReference() : null)
                    .depotNom(ligne.getPiece() != null && ligne.getPiece().getDepot() != null ? ligne.getPiece().getDepot().getNom() : null)
                    .quantite(ligne.getQuantite()).prix(ligne.getPrix())
                    .montantTotal(ligne.getQuantite() * ligne.getPrix()).build()).toList();
        }
        return commande.getLignes().stream().map(ligneCommande -> {
            var ligneReception = lignesReception.stream().filter(l -> l.getPiece() != null && ligneCommande.getPieceDetachee() != null
                    && l.getPiece().getId().equals(ligneCommande.getPieceDetachee().getId())).findFirst().orElse(null);
            int commandee = ligneCommande.getQuantite() == null ? 0 : ligneCommande.getQuantite();
            int recue = ligneCommande.getQuantiteRecue() == null ? 0 : ligneCommande.getQuantiteRecue();
            int recueDansCeBon = ligneReception == null || ligneReception.getQuantite() == null ? 0 : ligneReception.getQuantite();
            int prix = ligneCommande.getPrixUnitaire() == null ? 0 : ligneCommande.getPrixUnitaire().intValue();
            var piece = ligneCommande.getPieceDetachee();
            return LigneFacturationPieceResponse.builder()
                    .id(ligneReception != null ? ligneReception.getId() : ligneCommande.getId())
                    .pieceId(piece != null ? piece.getId() : null)
                    .designationPiece(piece != null ? piece.getDesignation() : ligneCommande.getDesignationPds())
                    .referencePiece(piece != null ? piece.getReference() : ligneCommande.getReferencePds())
                    .depotNom(piece != null && piece.getDepot() != null ? piece.getDepot().getNom() : null)
                    .quantite(recueDansCeBon).prix(prix).montantTotal(recueDansCeBon * prix)
                    .quantiteCommandee(commandee).quantiteRecueTotale(recue)
                    .quantiteRestante(Math.max(0, commandee - recue)).build();
        }).toList();
    }
}
