package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepDiagnosticDto extends BaseStepDto {
    private Double kilometrage;
    private Double niveauCarburant;
    private String remarquesTechnicien;
    private Long technicienId;
    private List<String> defautsConstates;
}
