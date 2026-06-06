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
}
