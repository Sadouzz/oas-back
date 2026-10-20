package sn.oas.facturation.features.ordreTravail.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.ordreReparation.dto.responses.ClientHeaderDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.VehiculeHeaderDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdreTravailSummaryDto {
    private Long id;
    private String numero;
    private String statut;
    private String statutLabel;
    private Integer etape;

    private Long ordreReparationId;
    private String ordreReparationNumero;
    private Long proformaId;
    private String proformaNumero;

    // Objets imbriqués (standard)
    private VehiculeHeaderDto vehicule;
    private ClientHeaderDto client;

    private Integer nombreTechniciens;
    private Integer nombreBonsCommande;
    private Integer nombreBonsSortie;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
