package sn.oas.facturation.garage.dto;

import lombok.Data;

@Data
public class GarageRequest {
    private String libelle;
    private String ville;
    private String adresse;
    private String contact;
}
