package sn.oas.facturation.features.recu.service;

import java.util.List;

import sn.oas.facturation.features.recu.dto.RecuRequest;
import sn.oas.facturation.features.recu.dto.RecuResponse;

import sn.oas.facturation.features.auth.data.entity.Client;

public interface RecuService {
    RecuResponse create(RecuRequest request);
    List<RecuResponse> getByFacture(Long factureId);
    List<RecuResponse> getClientRecus(Client client);
    List<RecuResponse> getAll();
}
