package sn.oas.facturation.features.ordreTravail.dto.steps;

import lombok.Data;
import sn.oas.facturation.features.ordreTravail.data.enums.StatutOrdreTravail;

@Data
public class BaseOtStepDto {
    private Long ordreTravailId;
    private String numero;
    private Long vehiculeId;
    private StatutOrdreTravail statut;
}
