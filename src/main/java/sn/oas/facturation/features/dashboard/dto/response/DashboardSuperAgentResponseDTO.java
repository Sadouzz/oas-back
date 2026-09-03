package sn.oas.facturation.features.dashboard.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record DashboardSuperAgentResponseDTO(
        long totalClients,
        long totalVehicules,
        long totalRupturesDeStock,
        long totalBonsDeSortieEnAttente,
        EtatOrdreReparationDTO etatOrdresReparation,
        List<OrdreReparationRecentDTO> ordresRecents,
        List<ClientRecentDTO> clientsRecents,
        List<AlerteStockRecentDTO> alertesStock
) {}
