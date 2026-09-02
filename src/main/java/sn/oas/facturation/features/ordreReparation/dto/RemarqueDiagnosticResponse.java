package sn.oas.facturation.features.ordreReparation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemarqueDiagnosticResponse {
    private Long id;
    private Long ordreReparationId;
    private String technicienNom;
    private String contenu;
    private LocalDateTime createdAt;
}
