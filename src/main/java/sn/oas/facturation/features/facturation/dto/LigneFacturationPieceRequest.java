package sn.oas.facturation.features.facturation.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class LigneFacturationPieceRequest {
    private Long pieceId;
    private Integer quantite;
    private Integer prix;
    private Boolean isCustom;
    private String designationPds;
    @DecimalMin(value = "0.0", message = "Le taux de remise doit être compris entre 0 et 100 %")
    @DecimalMax(value = "100.0", message = "Le taux de remise doit être compris entre 0 et 100 %")
    private Double tauxRemise;
}
