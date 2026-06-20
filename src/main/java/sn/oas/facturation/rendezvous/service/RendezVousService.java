package sn.oas.facturation.rendezvous.service;

import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.rendezvous.data.enums.RendezVousStatus;
import sn.oas.facturation.rendezvous.dto.RendezVousRequest;
import sn.oas.facturation.rendezvous.dto.RendezVousResponse;

import java.util.List;

public interface RendezVousService {
    RendezVousResponse bookRendezVous(Client client, RendezVousRequest request);
    RendezVousResponse cancelRendezVous(Client client, Long id);
    List<RendezVousResponse> getClientRendezVous(Client client);
    List<RendezVousResponse> getClientRendezVousByStatus(Client client, RendezVousStatus status);
    List<RendezVousResponse> getAllRendezVous();
    RendezVousResponse updateRendezVousStatus(Long id, RendezVousStatus status, String commentaire);
    RendezVousResponse validerRendezVous(Long id, List<Long> mecanicienIds);
}
