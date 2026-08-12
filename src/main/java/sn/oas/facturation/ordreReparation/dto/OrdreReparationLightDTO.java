package sn.oas.facturation.ordreReparation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.ordreReparation.data.enums.StatutOrdreReparation;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdreReparationLightDTO {
    private Long id;
    private String numero;
    private String descriptionTravaux;
    private LocalDateTime dateCreation;
    private LocalDateTime dateSortie;
    private StatutOrdreReparation statut;
    private VehiculeLightDTO vehicule;
}
