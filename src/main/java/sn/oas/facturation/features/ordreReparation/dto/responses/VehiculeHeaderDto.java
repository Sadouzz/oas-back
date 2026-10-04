package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;

@Data
public class VehiculeHeaderDto {
    private Long id;
    private String immatriculation;
    private String marque;
    private String modele;
    private Integer kilometrage;
    private ClientHeaderDto client;
}
