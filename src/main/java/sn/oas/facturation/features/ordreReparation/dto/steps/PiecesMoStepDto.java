package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PiecesMoStepDto {
    private Long ordreReparationId;
    private List<LignePieceDto> lignesPieces;
    private List<LigneMODto> lignesMainDoeuvres;
    private BigDecimal totalHtEstime;
    private BigDecimal totalTtcEstime;
}
