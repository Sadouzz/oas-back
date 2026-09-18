package sn.oas.facturation.features.ordreReparation.dto.steps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProformaStepDto {
    private Long ordreReparationId;
    private Long clientId;
    private Long vehiculeId;
    private Integer kilometrage;
    private BigDecimal tvaRate; // ex: 18%
    private BigDecimal montantTimbre;
    private BigDecimal montantAutre;
    private Boolean valideParClient;
    private LocalDateTime dateValidation;
}
