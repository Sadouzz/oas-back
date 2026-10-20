package sn.oas.facturation.features.ordreTravail.dto.responses;

import lombok.Data;

@Data
public class BaseOtStepResponseDto {
    private Long id;
    private String numero;
    private String statut;
    private Integer etape;
    private Long vehiculeId;
    private Long proformaId;
    private Long ordreReparationId;
}
