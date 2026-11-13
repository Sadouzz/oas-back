package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.facture.dto.FactureResponse;
import sn.oas.facturation.features.noteDePrix.dto.NoteDePrixResponse;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class StepFacturationResponseDto extends BaseStepResponseDto {
    private String remarquesPreparation;
    private String typeDocument; // "FACTURE", "NOTE_DE_PRIX" ou null
    private boolean hasDocument;
    private FactureResponse facture;
    private NoteDePrixResponse noteDePrix;
}

