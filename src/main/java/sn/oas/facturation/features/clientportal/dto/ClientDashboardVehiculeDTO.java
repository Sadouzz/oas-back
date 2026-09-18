package sn.oas.facturation.features.clientportal.dto;

import java.time.LocalDateTime;

public record ClientDashboardVehiculeDTO(
        Long id,
        String immatriculation,
        String marque,
        String modele,
        Integer annee,
        Double kilometrage,
        String numeroChassis,
        LocalDateTime createdAt,
        String stage,
        int stageIndex,
        String stageLabel,
        String stageTone,
        String ordreReparationNumero,
        Long ordreReparationId
) {}
