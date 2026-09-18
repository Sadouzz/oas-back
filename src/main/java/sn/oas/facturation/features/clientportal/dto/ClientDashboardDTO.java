package sn.oas.facturation.features.clientportal.dto;

import java.util.List;

public record ClientDashboardDTO(
        ClientStatsDTO stats,
        List<ClientDashboardVehiculeDTO> vehicules
) {}
