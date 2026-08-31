package sn.oas.facturation.bonDeReception.service;

import sn.oas.facturation.bonDeReception.dto.BonDeReceptionCreateRequest;
import sn.oas.facturation.bonDeReception.dto.BonDeReceptionResponse;
import sn.oas.facturation.bonDeReception.dto.BonDeReceptionUpdateRequest;

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
