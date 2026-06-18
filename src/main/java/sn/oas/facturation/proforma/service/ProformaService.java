package sn.oas.facturation.proforma.service;

import sn.oas.facturation.facture.dto.FactureResponse;
import sn.oas.facturation.proforma.dto.ProformaCreateRequest;
import sn.oas.facturation.proforma.dto.ProformaResponse;
import sn.oas.facturation.proforma.dto.ProformaUpdateRequest;

import java.util.List;

public interface ProformaService {
    ProformaResponse create(ProformaCreateRequest request);
    ProformaResponse update(Long id, ProformaUpdateRequest request);
    ProformaResponse getById(Long id);
    List<ProformaResponse> getAll();
    List<ProformaResponse> search(String keyword);
    List<ProformaResponse> getRecentProformas();
    ProformaResponse getByFicheAtelierId(Long ficheAtelierId);
    void delete(Long id);

    ProformaResponse valider(Long id);
    byte[] generatePdf(Long id);
    FactureResponse convertToFacture(Long id);
}
