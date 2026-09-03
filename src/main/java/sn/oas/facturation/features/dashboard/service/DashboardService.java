package sn.oas.facturation.features.dashboard.service;

import sn.oas.facturation.features.dashboard.dto.response.DashboardAgentMagasinResponse;
import sn.oas.facturation.features.dashboard.dto.response.DashboardAgentResponse;
import sn.oas.facturation.features.dashboard.dto.response.DashboardChefAtelierResponse;
import sn.oas.facturation.features.dashboard.dto.response.DashboardSuperAgentResponseDTO;

public interface DashboardService {
    DashboardSuperAgentResponseDTO getSuperAgentDashboard();
    DashboardAgentResponse getAgentDashboard();
    DashboardChefAtelierResponse getChefAtelierDashboard();
    DashboardAgentMagasinResponse getAgentMagasinDashboard();
}
