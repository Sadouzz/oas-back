package sn.oas.facturation.features.avoirTTC.service;

import org.springframework.data.domain.Page;
import sn.oas.facturation.features.avoirTTC.data.entity.AvoirTTC;
import sn.oas.facturation.features.avoirTTC.dto.AvoirTTCCreateRequest;

import java.util.List;

public interface AvoirTTCService {
    AvoirTTC create(AvoirTTCCreateRequest request);
    AvoirTTC getById(Long id);

    Page<AvoirTTC> getAll(int page, int size);
    List<AvoirTTC> getAll();

    List<AvoirTTC> search(String keyword);
    Page<AvoirTTC> search(String keyword, int page, int size);

    List<AvoirTTC> getRecentAvoirs();

    void delete(Long id);

    byte[] generatePdf(Long id);
}
