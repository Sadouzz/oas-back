package sn.oas.facturation.marketplace.service;

import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.marketplace.data.entity.DemandeProduit;
import sn.oas.facturation.marketplace.dto.DemandeProduitRequest;

import java.util.List;

public interface DemandeProduitService {
    DemandeProduit create(Client client, DemandeProduitRequest request);

    DemandeProduit updateStatus(Long id, String statut);

    DemandeProduit getById(Long id);

    DemandeProduit getByIdAndClient(Long id, Client client);

    DemandeProduit cancel(Long id, Client client);

    List<DemandeProduit> getByClient(Client client);

    List<DemandeProduit> getAll();

    List<DemandeProduit> getByProduit(Long produitId);

    List<DemandeProduit> getHistoriqueByClient(Client client);

    List<DemandeProduit> getHistoriqueTous();
}
