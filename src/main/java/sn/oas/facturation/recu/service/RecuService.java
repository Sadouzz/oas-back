package sn.oas.facturation.recu.service;

import java.util.List;

import sn.oas.facturation.recu.dto.RecuRequest;
import sn.oas.facturation.recu.dto.RecuResponse;

import sn.oas.facturation.auth.data.entity.Client;

public interface RecuService {
    RecuResponse create(RecuRequest request);
    List<RecuResponse> getByFacture(Long factureId);
    List<RecuResponse> getClientRecus(Client client);
    List<RecuResponse> getAll();
}
