package sn.oas.facturation.features.bonDeCommande.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LigneBonDeCommandeResponse {

    private Long id;

    private Long pieceDetacheeId;

    private String designationPiece;

    private String reference;

    private String categorie;

    private Integer quantite;

    private Integer quantiteRecue;

    private Double prixUnitaire;

    private Double montant;
}