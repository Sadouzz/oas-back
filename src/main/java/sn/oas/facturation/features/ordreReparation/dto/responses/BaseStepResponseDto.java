package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;

@Data
public class BaseStepResponseDto {
    private Long id;
    private String numero;
    private String statut;
    private Long vehiculeId;
}
