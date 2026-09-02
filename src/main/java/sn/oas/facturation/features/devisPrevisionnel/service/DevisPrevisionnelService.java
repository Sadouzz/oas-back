package sn.oas.facturation.features.devisPrevisionnel.service;

import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.devisPrevisionnel.data.entity.DevisPrevisionnel;
import sn.oas.facturation.features.devisPrevisionnel.dto.DevisPrevisionnelRequest;

import org.springframework.data.domain.Page;
import java.util.List;

public interface DevisPrevisionnelService {
    DevisPrevisionnel creer(DevisPrevisionnelRequest request);

    DevisPrevisionnel modifier(Long id, DevisPrevisionnelRequest request);

    void supprimer(Long id);

    DevisPrevisionnel getById(Long id);

    Page<DevisPrevisionnel> getAll(int page, int size);

    List<DevisPrevisionnel> getAll();

    List<DevisPrevisionnel> getByClient(Long clientId);

    List<DevisPrevisionnel> getByVehicule(Long vehiculeId);

    java.util.Optional<DevisPrevisionnel> getByFicheAtelierId(Long ficheAtelierId);

    List<DevisPrevisionnel> getClientDevis(Client client);

    List<DevisPrevisionnel> search(String keyword);

    byte[] generatePdf(Long id);

    DevisPrevisionnel valider(Long id);

    DevisPrevisionnel annuler(Long id);

    DevisPrevisionnel clientAccepter(Client client, Long id);

    DevisPrevisionnel clientRefuser(Client client, Long id);
}
