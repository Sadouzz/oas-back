package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneReceptionOrdre;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneTravailOrdre;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceptionStepDto {
    private String numero;
    private Long vehiculeId;
    private Integer kilometrageEntree;
    private LocalDateTime dateSortiePrevue;
    private String descriptionTravaux;
    private List<String> travauxDemandes;
    private List<LigneTravailOrdre> lignesTravaux;
    private List<LigneReceptionOrdre> lignesReception;
}
