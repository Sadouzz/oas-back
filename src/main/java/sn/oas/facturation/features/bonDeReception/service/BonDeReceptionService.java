package sn.oas.facturation.features.bonDeReception.service;

import org.springframework.data.domain.Page;
import sn.oas.facturation.features.bonDeReception.data.entity.BonDeReception;
import sn.oas.facturation.features.bonDeReception.dto.BonDeReceptionCreateRequest;
import sn.oas.facturation.features.bonDeReception.dto.BonDeReceptionUpdateRequest;

import java.util.List;

public interface BonDeReceptionService {
    BonDeReception create(BonDeReceptionCreateRequest request);
    BonDeReception update(Long id, BonDeReceptionUpdateRequest request);
    BonDeReception getById(Long id);
    Page<BonDeReception> getAll(int page, int size);
    List<BonDeReception> getAll();
    List<BonDeReception> search(String keyword);
    Page<BonDeReception> search(String keyword, int page, int size);
    List<BonDeReception> getRecentBonsDeReception();
    void delete(Long id);
    byte[] generatePdf(Long id);
}
