package sn.oas.facturation.avoirTTC.service;

import sn.oas.facturation.avoirTTC.dto.AvoirTTCResponse;

import java.util.List;

public interface AvoirTTCService {
    AvoirTTCResponse getById(Long id);
    List<AvoirTTCResponse> getAll();
    List<AvoirTTCResponse> search(String keyword);
    List<AvoirTTCResponse> getRecentAvoirs();
    void delete(Long id);
    byte[] generatePdf(Long id);
}
