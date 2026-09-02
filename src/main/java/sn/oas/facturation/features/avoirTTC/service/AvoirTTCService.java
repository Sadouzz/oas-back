package sn.oas.facturation.features.avoirTTC.service;

import sn.oas.facturation.features.avoirTTC.dto.AvoirTTCCreateRequest;
import sn.oas.facturation.features.avoirTTC.dto.AvoirTTCResponse;

import java.util.List;

public interface AvoirTTCService {
    AvoirTTCResponse create(AvoirTTCCreateRequest request);
    AvoirTTCResponse getById(Long id);

    List<AvoirTTCResponse> getAll();

    List<AvoirTTCResponse> search(String keyword);

    List<AvoirTTCResponse> getRecentAvoirs();

    void delete(Long id);

    byte[] generatePdf(Long id);
}
