package sn.oas.facturation.features.clientportal.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ClientVehiculeCardDTO(
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
        boolean hasActiveRepair,
        FicheEnCoursSummaryDTO ficheEnCours,
        List<ClientInterventionSummaryDTO> historique
) {}
