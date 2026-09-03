package sn.oas.facturation.features.dashboard.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record DashboardChefAtelierResponse(
        long totalBonsDeSortieEnAttente,
        long totalVehicules,
        EtatOrdreReparationDTO etatOrdresReparation,
        List<BonDeSortieEnAttenteDTO> bonsDeSortieEnAttenteValidation
) {}
