package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;

@Data
public class FactureSummaryDto {
    private Long id;
    private String statut;
    private Double montantRestant;
}
