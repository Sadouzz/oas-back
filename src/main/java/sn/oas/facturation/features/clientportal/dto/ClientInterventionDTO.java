package sn.oas.facturation.features.clientportal.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ClientInterventionDTO(
        Long id,
        String numero,
        String statut,
        String stageLabel,
        String stageTone,
        LocalDateTime dateCreation,
        LocalDateTime dateSortie,
        String listeDefauts,
        String descriptionTravaux,
        List<LigneReceptionItemDTO> lignesReception,
        VehiculeInfoDTO vehicule
) {
    public record VehiculeInfoDTO(
            Long id,
            String immatriculation,
            String marque,
            String modele
    ) {}
}
