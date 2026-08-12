package sn.oas.facturation.partenaire.service;

import java.util.List;
import sn.oas.facturation.partenaire.dto.PartenaireRequest;
import sn.oas.facturation.partenaire.dto.PartenaireResponse;

public interface PartenaireService {
    List<PartenaireResponse> getAllPartenaires();
    PartenaireResponse getPartenaireById(Long id);
    PartenaireResponse createPartenaire(PartenaireRequest request);
    PartenaireResponse updatePartenaire(Long id, PartenaireRequest request);
    void deletePartenaire(Long id);
    PartenaireResponse archivePartenaire(Long id);
    PartenaireResponse unarchivePartenaire(Long id);
}
