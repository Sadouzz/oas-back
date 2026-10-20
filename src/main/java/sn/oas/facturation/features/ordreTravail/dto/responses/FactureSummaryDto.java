package sn.oas.facturation.features.ordreTravail.dto.responses;

import lombok.Data;

@Data
public class FactureSummaryDto {
    private Long id;
    private String client;
    private String vehicule;
    private String statut;
    private Double montantAPayer;
    private Double montantPaye;
    private Double montantRestant;
}
