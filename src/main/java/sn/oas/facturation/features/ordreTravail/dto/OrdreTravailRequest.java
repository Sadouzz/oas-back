package sn.oas.facturation.features.ordreTravail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.ordreTravail.data.enums.StatutOrdreTravail;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdreTravailRequest {
    private Long clientId;
    private Long vehiculeId;
    private Long proformaId;
    private Long ordreReparationId;
    private StatutOrdreTravail statut;
    private List<Long> technicienIds;
    private List<Long> bonDeCommandeIds;
    private List<Long> bonDeSortieIds;
}
