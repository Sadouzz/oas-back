package sn.oas.facturation.features.rendezvous.service;

import org.springframework.data.domain.Page;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.features.rendezvous.data.enums.RendezVousStatus;
import sn.oas.facturation.features.rendezvous.dto.RendezVousRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface RendezVousService {
    RendezVous bookRendezVous(Client client, RendezVousRequest request);
    RendezVous cancelRendezVous(Client client, Long id);
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
    RendezVous validerRendezVous(Long id, List<Long> mecanicienIds);
    RendezVous getById(Long id);
    RendezVous updateDate(Long id, LocalDateTime nouvelleDate);
}
