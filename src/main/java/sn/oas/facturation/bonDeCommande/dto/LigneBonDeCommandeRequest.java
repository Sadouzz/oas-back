package sn.oas.facturation.bonDeCommande.dto;

import lombok.Data;
import sn.oas.facturation.piecedetache.data.enums.TypePiece;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


@Data
public class LigneBonDeCommandeRequest {

    // Cas PDP ou PDG existant
    private Long pieceDetacheeId;

    // Cas nouvelle pièce
    private TypePiece typePiece;

    // PDP ou PDG à créer
    private String reference;
    private String designation;
    private String categorie;
    // private Double pourcentage;

    // Cas PDS
    private String designationPds;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantite;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @Min(value = 0, message = "Le prix unitaire ne peut pas être négatif")
    private Double prixUnitaire;
}