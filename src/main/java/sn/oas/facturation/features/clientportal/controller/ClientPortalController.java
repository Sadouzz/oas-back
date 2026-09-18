package sn.oas.facturation.features.clientportal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.clientportal.dto.*;
import sn.oas.facturation.features.clientportal.service.ClientPortalService;

import java.util.List;

@RestController
@RequestMapping("/api/client-portal")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('CLIENT', 'ROLE_CLIENT')")
@Tag(name = "Portail Client (BFF)", description = "Endpoints optimisés dédiés à l'espace client")
public class ClientPortalController {

    private final ClientPortalService clientPortalService;

    @GetMapping("/dashboard")
    @Operation(summary = "Données consolidées pour le tableau de bord client")
    public ResponseEntity<ClientDashboardDTO> getDashboard() {
        return ResponseEntity.ok(clientPortalService.getDashboard());
    }

    @GetMapping("/vehicules")
    @Operation(summary = "Liste des véhicules du client avec état d'intervention et fiche en cours résolus")
    public ResponseEntity<List<ClientVehiculeCardDTO>> getMyVehicules() {
        return ResponseEntity.ok(clientPortalService.getMyVehicules());
    }

    @GetMapping("/vehicules/{id}/historique")
    @Operation(summary = "Historique des réparations d'un véhicule spécifique du client connecté")
    public ResponseEntity<List<ClientInterventionSummaryDTO>> getVehiculeHistorique(@PathVariable Long id) {
        return ResponseEntity.ok(clientPortalService.getVehiculeHistorique(id));
    }

    @GetMapping("/interventions")
    @Operation(summary = "Historique léger des interventions du client connecté")
    public ResponseEntity<List<ClientInterventionDTO>> getMyInterventions() {
        return ResponseEntity.ok(clientPortalService.getMyInterventions());
    }

    @GetMapping("/rendez-vous/booking-context")
    @Operation(summary = "Contexte pour la réservation de rendez-vous (véhicules disponibles et garages)")
    public ResponseEntity<ClientBookingContextDTO> getBookingContext() {
        return ResponseEntity.ok(clientPortalService.getBookingContext());
    }
}
