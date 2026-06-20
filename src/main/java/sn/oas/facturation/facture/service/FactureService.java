package sn.oas.facturation.facture.service;

import sn.oas.facturation.facture.dto.FactureResponse;

import java.util.List;

public interface FactureService {
    FactureResponse getById(Long id);
    List<FactureResponse> getAll();
    List<FactureResponse> search(String keyword);
    List<FactureResponse> getRecentFactures();
    void delete(Long id);
    byte[] generatePdf(Long id);
    FactureResponse createFacture(sn.oas.facturation.facture.dto.FactureCreateRequest request);
    FactureResponse createFactureAuto(sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier fiche);
    
    // Client methods
    List<FactureResponse> getClientFactures(sn.oas.facturation.auth.data.entity.Client client);
    FactureResponse getClientFactureById(sn.oas.facturation.auth.data.entity.Client client, Long id);
}
