package sn.oas.facturation.features.facturation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LigneFacturationMainDoeuvreResponse {
    private Long id;
    private Long mainDoeuvreId;
    private String descriptionMainDoeuvre;
    private Integer nbreHeure;
    private Integer tarifHoraire;
    private Integer montantTotal;
}
