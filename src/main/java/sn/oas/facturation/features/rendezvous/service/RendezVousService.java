package sn.oas.facturation.features.rendezvous.service;

import org.springframework.data.domain.Page;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.features.rendezvous.data.enums.RendezVousStatus;
import sn.oas.facturation.features.rendezvous.dto.RendezVousRequest;
import sn.oas.facturation.features.rendezvous.dto.RendezVousCreationResult;
import sn.oas.facturation.features.rendezvous.dto.DemandeDerogationPlafondResponse;
import sn.oas.facturation.features.clientportal.dto.ReservationBookingStatusDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface RendezVousService {
    RendezVousCreationResult bookRendezVous(Client client, RendezVousRequest request, String requestedBy);
    RendezVousCreationResult createRendezVousByAdmin(RendezVousRequest request, String requestedBy, String requesterRole);
    Page<DemandeDerogationPlafondResponse> getPendingQuotaDerogations(int page, int size);
    RendezVous approveQuotaDerogation(Long requestId, String decisionMaker, String reason);
    DemandeDerogationPlafondResponse rejectQuotaDerogation(Long requestId, String decisionMaker, String reason);
    ReservationBookingStatusDTO getClientBookingStatus(Long clientId);
    RendezVous cancelRendezVous(Client client, Long id);
    RendezVous cancelRendezVous(Client client, Long id, String motifAnnulation);
    List<RendezVous> getClientRendezVous(Client client);
    Page<RendezVous> getClientRendezVous(Client client, int page, int size);
    List<RendezVous> getRendezVousByClientId(Long clientId);
    Page<RendezVous> getRendezVousByClientId(Long clientId, int page, int size);
    List<RendezVous> getClientRendezVousByStatus(Client client, RendezVousStatus status);
    List<RendezVous> getAllRendezVous();
    Page<RendezVous> getAllRendezVous(int page, int size);
    Page<RendezVous> searchRendezVous(String keyword, int page, int size);
    Page<RendezVous> getByStatut(RendezVousStatus status, int page, int size);
    RendezVous updateRendezVousStatus(Long id, RendezVousStatus status, String commentaire);
    RendezVous updateRendezVousStatus(Long id, RendezVousStatus status, String commentaire, String motifAnnulation);
    RendezVous validerRendezVous(Long id, List<Long> mecanicienIds);
    RendezVous updateRendezVous(Long id, RendezVousRequest request);
    RendezVous getById(Long id);
    RendezVous updateDate(Long id, LocalDateTime nouvelleDate);
}
