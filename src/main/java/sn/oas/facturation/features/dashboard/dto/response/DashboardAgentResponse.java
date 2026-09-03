package sn.oas.facturation.features.dashboard.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record DashboardAgentResponse(
        long totalClients,
        long totalVehicules,
        long totalBonsDeSortieEnAttente,
        EtatOrdreReparationDTO etatOrdresReparation,
        List<ClientRecentDTO> clientsRecents,
        List<BonDeSortieEnAttenteDTO> bonsDeSortieEnAttente
) {}
