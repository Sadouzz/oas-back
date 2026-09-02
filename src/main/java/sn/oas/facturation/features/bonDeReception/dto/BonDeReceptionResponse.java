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
    private BigDecimal montantTTC;
    private BigDecimal montantTimbre;
    private BigDecimal montantTotal;
    private Long agentId;
    private String agentNom;
    private String remarque;
    private Double kilometrage;
    
    private Long bonDeCommandeId;
    private String bonDeCommandeNumero;
    
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
                .montantTTC(bl.getMontantTTC())
                .montantTimbre(bl.getMontantTimbre())
                .montantTotal(bl.getMontantTotal())
                .agentId(bl.getAgent() != null ? bl.getAgent().getId() : null)
                .agentNom(bl.getAgent() != null ? (bl.getAgent().getFirstName() + " " + bl.getAgent().getLastName()).trim() : null)
                .remarque(bl.getRemarque())
                .kilometrage(bl.getKilometrage())
                .bonDeCommandeId(bl.getBonDeCommande() != null ? bl.getBonDeCommande().getId() : null)
                .bonDeCommandeNumero(bl.getBonDeCommande() != null ? bl.getBonDeCommande().getNumero() : null)
                .lignesPieces(bl.getLignesFacturationPieces() == null ? List.of() : bl.getLignesFacturationPieces().stream()
                        .map(ligne -> LigneFacturationPieceResponse.builder()
                                .id(ligne.getId())
                                .pieceId(ligne.getPiece() != null ? ligne.getPiece().getId() : null)
                                .designationPiece(ligne.getPiece() != null ? ligne.getPiece().getDesignation() : null)
                                .quantite(ligne.getQuantite())
                                .prix(ligne.getPrix())
                                .montantTotal(ligne.getQuantite() * ligne.getPrix())
                                .build())
                        .toList())
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
}
