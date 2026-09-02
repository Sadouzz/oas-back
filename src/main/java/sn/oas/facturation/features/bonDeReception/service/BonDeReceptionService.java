package sn.oas.facturation.features.bonDeReception.service;

import sn.oas.facturation.features.bonDeReception.dto.BonDeReceptionCreateRequest;
import sn.oas.facturation.features.bonDeReception.dto.BonDeReceptionResponse;
import sn.oas.facturation.features.bonDeReception.dto.BonDeReceptionUpdateRequest;

import java.util.List;

public interface BonDeReceptionService {
    BonDeReceptionResponse create(BonDeReceptionCreateRequest request);
    BonDeReceptionResponse update(Long id, BonDeReceptionUpdateRequest request);
    BonDeReceptionResponse getById(Long id);
    List<BonDeReceptionResponse> getAll();
    List<BonDeReceptionResponse> search(String keyword);
    List<BonDeReceptionResponse> getRecentBonsDeReception();
    void delete(Long id);
    byte[] generatePdf(Long id);
}
