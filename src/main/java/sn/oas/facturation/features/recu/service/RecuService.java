package sn.oas.facturation.features.recu.service;

import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.recu.data.entity.Recu;
import sn.oas.facturation.features.recu.dto.RecuRequest;

import java.util.List;

public interface RecuService {
    Recu create(RecuRequest request);
    List<Recu> getByFacture(Long factureId);
    List<Recu> getClientRecus(Client client);
    List<Recu> getAll();
}
