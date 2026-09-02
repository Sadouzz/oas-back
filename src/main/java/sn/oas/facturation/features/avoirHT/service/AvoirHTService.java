package sn.oas.facturation.features.avoirHT.service;

import org.springframework.data.domain.Page;
import sn.oas.facturation.features.avoirHT.data.entity.AvoirHT;
import sn.oas.facturation.features.avoirHT.dto.AvoirHTCreateRequest;

import java.util.List;

public interface AvoirHTService {
    AvoirHT create(AvoirHTCreateRequest request);
    AvoirHT getById(Long id);

    Page<AvoirHT> getAll(int page, int size);
    List<AvoirHT> getAll();

    List<AvoirHT> search(String keyword);
    Page<AvoirHT> search(String keyword, int page, int size);

    List<AvoirHT> getRecentAvoirs();

    void delete(Long id);

    byte[] generatePdf(Long id);
}
