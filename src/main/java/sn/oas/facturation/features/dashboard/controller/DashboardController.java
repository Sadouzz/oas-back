package sn.oas.facturation.features.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.oas.facturation.features.dashboard.dto.response.DashboardAgentMagasinResponse;
import sn.oas.facturation.features.dashboard.dto.response.DashboardAgentResponse;
import sn.oas.facturation.features.dashboard.dto.response.DashboardChefAtelierResponse;
import sn.oas.facturation.features.dashboard.dto.response.DashboardSuperAgentResponseDTO;
import sn.oas.facturation.features.dashboard.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints pour les tableaux de bord")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Tableau de bord Super Agent", description = "Retourne les KPIs, états des ordres, ordres récents, clients récents et alertes de stock.")
    @ApiResponse(responseCode = "200", description = "Données du tableau de bord retournées avec succès")
    @PreAuthorize("hasRole('SUPER_AGENT')")
    @GetMapping("/super-agent")
    public ResponseEntity<DashboardSuperAgentResponseDTO> getSuperAgentDashboard() {
        return ResponseEntity.ok(dashboardService.getSuperAgentDashboard());
    }

    @Operation(summary = "Tableau de bord Agent", description = "Retourne les KPIs agent (Clients, Véhicules, Bons en attente), états des ordres, clients récents et bons en attente.")
    @ApiResponse(responseCode = "200", description = "Données du tableau de bord agent retournées avec succès")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT')")
    @GetMapping("/agent")
    public ResponseEntity<DashboardAgentResponse> getAgentDashboard() {
        return ResponseEntity.ok(dashboardService.getAgentDashboard());
    }

    @Operation(summary = "Tableau de bord Chef d'Atelier", description = "Retourne les KPIs chef d'atelier (Bons à valider, Véhicules enregistrés), états des ordres et bons en attente de validation.")
    @ApiResponse(responseCode = "200", description = "Données du tableau de bord chef d'atelier retournées avec succès")
    @PreAuthorize("hasAnyRole('CHEF_ATELIER', 'SUPER_AGENT')")
    @GetMapping("/chef-atelier")
    public ResponseEntity<DashboardChefAtelierResponse> getChefAtelierDashboard() {
        return ResponseEntity.ok(dashboardService.getChefAtelierDashboard());
    }

    @Operation(summary = "Tableau de bord Agent Magasin", description = "Retourne les KPIs agent magasin (Bons à valider, Véhicules enregistrés), états des ordres et bons en attente de validation.")
    @ApiResponse(responseCode = "200", description = "Données du tableau de bord agent magasin retournées avec succès")
    @PreAuthorize("hasAnyRole('AGENT_MAGASIN', 'SUPER_AGENT', 'CHEF_ATELIER')")
    @GetMapping("/agent-magasin")
    public ResponseEntity<DashboardAgentMagasinResponse> getAgentMagasinDashboard() {
        return ResponseEntity.ok(dashboardService.getAgentMagasinDashboard());
    }
}
