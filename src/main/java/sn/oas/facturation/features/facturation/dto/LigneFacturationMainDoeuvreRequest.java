package sn.oas.facturation.features.facturation.dto;

import lombok.Data;

@Data
public class LigneFacturationMainDoeuvreRequest {
    private Long mainDoeuvreId;
    private Integer nbreHeure;
    private Integer tarifHoraire;
}
