package sn.oas.facturation.features.facturation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneFacturationPieceResponse {
    private Long id;
    private Long pieceId;
    private String type; // "PDP", "PDG", "PDS"
    private String designationPiece;
    private Boolean isCustom;
    private String designationPds;
    private String referencePiece;
    private String depotNom;
    private Integer quantite;
    private Integer prix;
    private Integer montantTotal;
    private Double tauxRemise;
    private Integer quantiteCommandee;
    private Integer quantiteRecueTotale;
    private Integer quantiteRestante;
    private List<LocalDateTime> datesHeuresSaisieSource;
}
