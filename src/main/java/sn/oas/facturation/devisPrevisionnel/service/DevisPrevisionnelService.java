package sn.oas.facturation.devisPrevisionnel.service;

import sn.oas.facturation.devisPrevisionnel.data.entity.DevisPrevisionnel;
import sn.oas.facturation.devisPrevisionnel.dto.DevisPrevisionnelRequest;

import java.util.List;

public interface DevisPrevisionnelService {
    DevisPrevisionnel creer(DevisPrevisionnelRequest request);

    DevisPrevisionnel modifier(Long id, DevisPrevisionnelRequest request);

    void supprimer(Long id);

    DevisPrevisionnel getById(Long id);

    List<DevisPrevisionnel> getAll();

    List<DevisPrevisionnel> getByClient(Long clientId);

    List<DevisPrevisionnel> getByVehicule(Long vehiculeId);

    List<DevisPrevisionnel> getClientDevis(sn.oas.facturation.auth.data.entity.Client client);

    DevisPrevisionnel clientAccepter(sn.oas.facturation.auth.data.entity.Client client, Long id);

    DevisPrevisionnel clientRefuser(sn.oas.facturation.auth.data.entity.Client client, Long id);
}
