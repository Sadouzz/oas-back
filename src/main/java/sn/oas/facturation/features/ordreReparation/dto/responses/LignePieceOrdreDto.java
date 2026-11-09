package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LignePieceOrdreDto {
    private Long id;
    private Integer quantite;
    private Integer prix;
    private Double tauxRemise;
    private Boolean isCustom;
    private String designationPds;
    private LocalDateTime dateHeureSaisie;
    private Long pieceId; 
    private PieceSummaryDto piece; 
}
