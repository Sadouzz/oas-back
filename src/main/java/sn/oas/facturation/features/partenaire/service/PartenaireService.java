package sn.oas.facturation.features.partenaire.service;

import org.springframework.data.domain.Page;
import java.util.List;
import sn.oas.facturation.features.partenaire.dto.PartenaireRequest;
import sn.oas.facturation.features.partenaire.dto.PartenaireResponse;

public interface PartenaireService {
    Page<PartenaireResponse> getAllPartenaires(int page, int size);
    List<PartenaireResponse> getAllPartenaires();
    PartenaireResponse getPartenaireById(Long id);
    PartenaireResponse createPartenaire(PartenaireRequest request);
    PartenaireResponse updatePartenaire(Long id, PartenaireRequest request);
    void deletePartenaire(Long id);
    PartenaireResponse archivePartenaire(Long id);
    PartenaireResponse unarchivePartenaire(Long id);
}
