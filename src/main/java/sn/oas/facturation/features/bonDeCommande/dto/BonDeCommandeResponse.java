package sn.oas.facturation.features.bonDeCommande.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BonDeCommandeResponse {

    private Long id;

    private String numero;

    private LocalDateTime dateCommande;

    private String statut;

    private Long fournisseurId;

    private String fournisseurNom;

    private Long vehiculeId;

    private String immatriculationVehicule;

    private BigDecimal montantHT;

    private BigDecimal montantTVA;

    private BigDecimal montantTTC;

    private Boolean tvaApplicable;

    private Boolean paye;

    private String observation;

    private List<LigneBonDeCommandeResponse> lignes;

    public static BonDeCommandeResponse from(sn.oas.facturation.features.bonDeCommande.data.entity.BonDeCommande bc) {
        if (bc == null) return null;
        return BonDeCommandeResponse.builder()
                .id(bc.getId())
                .numero(bc.getNumero())
                .dateCommande(bc.getDateCommande())
                .statut(bc.getStatut() != null ? bc.getStatut().name() : null)
                .fournisseurId(bc.getFournisseur() != null ? bc.getFournisseur().getId() : null)
                .fournisseurNom(bc.getFournisseur() != null ? bc.getFournisseur().getNomEntreprise() : null)
                .vehiculeId(bc.getVehicule() != null ? bc.getVehicule().getId() : null)
                .immatriculationVehicule(bc.getVehicule() != null ? bc.getVehicule().getImmatriculation() : null)
                .montantHT(bc.getMontantHT())
                .montantTVA(bc.getMontantTVA())
                .montantTTC(bc.getMontantTTC())
                .tvaApplicable(bc.getTvaApplicable())
                .paye(bc.getPaye())
                .observation(bc.getObservation())
                .lignes(bc.getLignes() == null ? List.of() : bc.getLignes().stream().map(ligne -> LigneBonDeCommandeResponse.builder()
                        .id(ligne.getId())
                        .pieceDetacheeId(ligne.getPieceDetachee() != null ? ligne.getPieceDetachee().getId() : null)
                        .designationPiece(ligne.getPieceDetachee() != null ? ligne.getPieceDetachee().getDesignation()
                                : ligne.getDesignationPds())
                        .reference(ligne.getPieceDetachee() != null ? ligne.getPieceDetachee().getReference()
                                : ligne.getReferencePds())
                        .categorie(ligne.getPieceDetachee() != null && ligne.getPieceDetachee().getCategorie() != null
                                ? ligne.getPieceDetachee().getCategorie().getNom()
                                : ligne.getCategoriePds())
                        .quantite(ligne.getQuantite())
                        .quantiteRecue(ligne.getQuantiteRecue())
                        .prixUnitaire(ligne.getPrixUnitaire() != null ? ligne.getPrixUnitaire().doubleValue() : 0.0)
                        .montant(ligne.getMontant() != null ? ligne.getMontant().doubleValue() : 0.0)
                        .build()).toList())
                .build();
    }
}