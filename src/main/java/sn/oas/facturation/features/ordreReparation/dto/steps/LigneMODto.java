package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.Data;

@Data
public class LigneMoDto {
    private Long mainDoeuvreId;
    private Integer nbreHeure;
    private Double prix;
}
