package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LivraisonStepDto {
    private Long ordreReparationId;
    private String nomReceptionnaire;
    private String telephoneReceptionnaire;
    private String pieceIdentiteNumero;
    private LocalDateTime dateLivraisonEffective;
    private Boolean restitutionConforme;
    private String signatureClientUrl;
}
