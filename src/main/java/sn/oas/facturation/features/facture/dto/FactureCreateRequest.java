package sn.oas.facturation.features.facture.dto;

import lombok.Data;

@Data
public class FactureCreateRequest {
    private Long clientId;
    private Long vehiculeId;
    private Long ordreReparationId;
    private Double kilometrage;
    private String remarque;
    private Boolean appliquerTVA;
    private Boolean appliquerTimbre;
    private String modePaiement;
}
