package sn.oas.facturation.features.dashboard.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record DashboardAgentMagasinResponse(
        long totalAlertes,
        long totalRuptures,
        long totalStocksFaibles,
        long totalBonsEnAttente,
        List<AlerteStockRecentDTO> rupturesDeStock,
        List<AlerteStockRecentDTO> stocksFaibles
) {}
