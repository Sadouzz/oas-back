package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BonDeSortieSummaryDto {
    private Long id;
    private String statut;
    private LocalDate dateSortiePrevue;
}
