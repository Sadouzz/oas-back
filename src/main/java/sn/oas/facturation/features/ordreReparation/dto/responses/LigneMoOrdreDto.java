package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LigneMoOrdreDto {
    private Long id;
    private Integer heures;
    private Integer prix;
    private Double tauxRemise;
    private String description; 
    private LocalDateTime dateHeureSaisie;
}
