package sn.oas.facturation.facture.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FactureCreateRequest {
    private Long clientId;
    private Long vehiculeId;
    private Long ficheAtelierId;
    private Double kilometrage;
    private String remarque;
    private Double tvaRate; // taux TVA en pourcentage (ex: 18.0)
    private BigDecimal montantTimbre; // montant timbre fiscal
    private BigDecimal montantAutre;
    private String modePaiement;
}
