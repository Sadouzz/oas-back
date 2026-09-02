package sn.oas.facturation.features.ordreReparation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeLightDTO {
    private Long id;
    private String immatriculation;
    private String marque;
    private String modele;
    private ClientLightDTO client;
}
