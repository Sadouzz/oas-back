package sn.oas.facturation.features.avoirHT.service;

import sn.oas.facturation.features.avoirHT.dto.AvoirHTCreateRequest;
import sn.oas.facturation.features.avoirHT.dto.AvoirHTResponse;

import java.util.List;

public interface AvoirHTService {
    AvoirHTResponse create(AvoirHTCreateRequest request);
    AvoirHTResponse getById(Long id);

    List<AvoirHTResponse> getAll();

    List<AvoirHTResponse> search(String keyword);

    List<AvoirHTResponse> getRecentAvoirs();

    void delete(Long id);

    byte[] generatePdf(Long id);
}
