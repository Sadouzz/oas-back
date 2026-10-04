package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;

@Data
public class PieceSummaryDto {
    private Long id;
    private String reference;
    private String designation;
    private String type; // "PDP" ou "PDG"
    private Double stockMagasin;
}
