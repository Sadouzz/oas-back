package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;

@Data
public class LigneMoOrdreDto {
    private Long id;
    private Integer heures;
    private Integer prix;
    private String description; 
}
