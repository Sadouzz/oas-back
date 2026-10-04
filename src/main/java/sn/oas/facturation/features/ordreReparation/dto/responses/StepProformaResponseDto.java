package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepProformaResponseDto extends BaseStepResponseDto {
    private ProformaSummaryDto proforma;
    private VehiculeHeaderDto vehicule;
    private DiagnosticDto diagnostic;
    private java.time.LocalDateTime updatedAt;
    private java.time.LocalDateTime dateCreation;

    @Data
    public static class DiagnosticDto {
        private Integer kilometrage;
    }
}
