package sn.oas.facturation.features.clientportal.service;

import sn.oas.facturation.features.clientportal.dto.*;

import java.util.List;

public interface ClientPortalService {
    ClientDashboardDTO getDashboard();
    List<ClientVehiculeCardDTO> getMyVehicules();
    List<ClientInterventionDTO> getMyInterventions();
    List<ClientInterventionSummaryDTO> getVehiculeHistorique(Long vehiculeId);
    ClientBookingContextDTO getBookingContext();
}
