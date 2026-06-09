package sn.oas.facturation.recu.service;

import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.recu.dto.RecuResponse;

import java.math.BigDecimal;
import java.util.List;

public interface RecuService {
    RecuResponse payerFacture(Long factureId, BigDecimal montant, String methodePaiement);
    List<RecuResponse> getClientRecus(Client client);
    List<RecuResponse> getAllRecus();
}
