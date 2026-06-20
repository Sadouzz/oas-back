package sn.oas.facturation.ficheAtelier.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.ficheAtelier.data.enums.StatutReparation;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FicheAtelierLightDTO {
    private Long id;
    private String numero;
    private String descriptionTravaux;
    private LocalDateTime dateCreation;
    private LocalDateTime dateSortie;
    private StatutReparation statut;
    private VehiculeLightDTO vehicule;
}
