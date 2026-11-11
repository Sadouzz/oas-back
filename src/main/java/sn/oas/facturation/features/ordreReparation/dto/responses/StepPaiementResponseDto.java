package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @deprecated Remplacé par {@link StepBLResponseDto} pour l'étape Bon de Livraison (BL).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Deprecated
public class StepPaiementResponseDto extends StepBLResponseDto {
}
