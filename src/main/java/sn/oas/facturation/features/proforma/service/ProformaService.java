package sn.oas.facturation.features.proforma.service;

import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.facture.dto.FactureResponse;
import sn.oas.facturation.features.proforma.dto.ProformaCreateRequest;
import sn.oas.facturation.features.proforma.dto.ProformaResponse;
import sn.oas.facturation.features.proforma.dto.ProformaUpdateRequest;

import org.springframework.data.domain.Page;
import java.util.List;

public interface ProformaService {
    ProformaResponse create(ProformaCreateRequest request);
    ProformaResponse update(Long id, ProformaUpdateRequest request);
    ProformaResponse getById(Long id);
    Page<ProformaResponse> getAll(int page, int size);
    List<ProformaResponse> getAll();
    List<ProformaResponse> search(String keyword);
    List<ProformaResponse> getRecentProformas();
    ProformaResponse getByOrdreReparationId(Long ordreReparationId);
    void delete(Long id);

    ProformaResponse valider(Long id);
    ProformaResponse validerEnvoi(Long id);
    byte[] generatePdf(Long id);
    FactureResponse convertToFacture(Long id);

    // Client methods
    List<ProformaResponse> getClientProformas(Client client);
    ProformaResponse clientValider(Client client, Long id);
    ProformaResponse clientRefuser(Client client, Long id);
}
