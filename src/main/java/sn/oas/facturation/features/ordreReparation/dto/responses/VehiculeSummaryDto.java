package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;

@Data
public class VehiculeSummaryDto {
    private Long id;
    private String immatriculation;
    private String marque;
    private String modele;
}
