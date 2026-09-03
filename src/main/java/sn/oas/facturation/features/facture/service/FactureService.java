package sn.oas.facturation.features.facture.service;

import org.springframework.data.domain.Page;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.dto.FactureCreateRequest;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;

import java.util.List;

public interface FactureService {
    Facture getById(Long id);
    Page<Facture> getAll(int page, int size);
    List<Facture> getAll();
    List<Facture> search(String keyword);
    Page<Facture> search(String keyword, int page, int size);
    List<Facture> getRecentFactures();
    void delete(Long id);
    byte[] generatePdf(Long id);
    Facture createFacture(FactureCreateRequest request);
    Facture createFactureAuto(OrdreReparation fiche);
    
    // Client methods
    List<Facture> getClientFactures(Client client);
    Facture getClientFactureById(Client client, Long id);
}
