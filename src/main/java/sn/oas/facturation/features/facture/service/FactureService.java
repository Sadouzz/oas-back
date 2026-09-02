package sn.oas.facturation.features.facture.service;

import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.facture.dto.FactureResponse;
import sn.oas.facturation.features.facture.dto.FactureCreateRequest;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;

import org.springframework.data.domain.Page;
import java.util.List;

public interface FactureService {
    FactureResponse getById(Long id);
    Page<FactureResponse> getAll(int page, int size);
    List<FactureResponse> getAll();
    List<FactureResponse> search(String keyword);
    List<FactureResponse> getRecentFactures();
    void delete(Long id);
    byte[] generatePdf(Long id);
    FactureResponse createFacture(FactureCreateRequest request);
    FactureResponse createFactureAuto(OrdreReparation fiche);
    
    // Client methods
    List<FactureResponse> getClientFactures(Client client);
    FactureResponse getClientFactureById(Client client, Long id);
}
