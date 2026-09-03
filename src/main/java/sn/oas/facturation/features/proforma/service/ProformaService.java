package sn.oas.facturation.features.proforma.service;

import org.springframework.data.domain.Page;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.proforma.data.entity.Proforma;
import sn.oas.facturation.features.proforma.dto.ProformaCreateRequest;
import sn.oas.facturation.features.proforma.dto.ProformaUpdateRequest;

import java.util.List;

public interface ProformaService {
    Proforma create(ProformaCreateRequest request);
    Proforma update(Long id, ProformaUpdateRequest request);
    Proforma getById(Long id);
    Page<Proforma> getAll(int page, int size);
    List<Proforma> getAll();
    List<Proforma> search(String keyword);
    Page<Proforma> search(String keyword, int page, int size);
    List<Proforma> getRecentProformas();
    Proforma getByOrdreReparationId(Long ordreReparationId);
    void delete(Long id);

    Proforma valider(Long id);
    Proforma validerEnvoi(Long id);
    byte[] generatePdf(Long id);
    Facture convertToFacture(Long id);

    // Client methods
    List<Proforma> getClientProformas(Client client);
    Proforma clientValider(Client client, Long id);
    Proforma clientRefuser(Client client, Long id);
}
