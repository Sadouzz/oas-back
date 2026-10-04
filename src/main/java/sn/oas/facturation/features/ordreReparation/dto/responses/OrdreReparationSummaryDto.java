package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;

@Data
public class OrdreReparationSummaryDto {
    private Long id;
    private String numero;
    private String statut;
    private Boolean hasDiagnostic; 
    private Boolean hasPiecesMo; 
    private VehiculeHeaderDto vehicule; 
}
