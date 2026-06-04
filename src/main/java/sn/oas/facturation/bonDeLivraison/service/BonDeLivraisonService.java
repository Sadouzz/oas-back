package sn.oas.facturation.bonDeLivraison.service;

import sn.oas.facturation.bonDeLivraison.dto.BonDeLivraisonCreateRequest;
import sn.oas.facturation.bonDeLivraison.dto.BonDeLivraisonResponse;
import sn.oas.facturation.bonDeLivraison.dto.BonDeLivraisonUpdateRequest;

import java.util.List;

public interface BonDeLivraisonService {
    BonDeLivraisonResponse create(BonDeLivraisonCreateRequest request);
    BonDeLivraisonResponse update(Long id, BonDeLivraisonUpdateRequest request);
    BonDeLivraisonResponse getById(Long id);
    List<BonDeLivraisonResponse> getAll();
    List<BonDeLivraisonResponse> search(String keyword);
    List<BonDeLivraisonResponse> getRecentBonsDeLivraison();
    void delete(Long id);
    byte[] generatePdf(Long id);
}
