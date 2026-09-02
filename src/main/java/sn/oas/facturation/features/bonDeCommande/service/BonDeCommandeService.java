package sn.oas.facturation.features.bonDeCommande.service;

import sn.oas.facturation.features.bonDeCommande.dto.BonDeCommandeCreateRequest;
import sn.oas.facturation.features.bonDeCommande.dto.BonDeCommandeResponse;
import sn.oas.facturation.features.bonDeCommande.dto.BonDeCommandeUpdateRequest;
import sn.oas.facturation.features.bonDeCommande.dto.ReceptionBonDeCommandeRequest;

import org.springframework.data.domain.Page;
import java.util.List;

public interface BonDeCommandeService {

    BonDeCommandeResponse create(BonDeCommandeCreateRequest request);

    BonDeCommandeResponse update(
            Long id,
            BonDeCommandeUpdateRequest request
    );

    BonDeCommandeResponse getById(Long id);

    Page<BonDeCommandeResponse> getAll(int page, int size);

    List<BonDeCommandeResponse> getAll();

    List<BonDeCommandeResponse> search(String keyword);

    List<BonDeCommandeResponse> getRecentBonDeCommandes();

    BonDeCommandeResponse envoyer(Long id);

    BonDeCommandeResponse receptionner(Long id);

    BonDeCommandeResponse receptionnerAvecQuantites(Long id, ReceptionBonDeCommandeRequest request);

    BonDeCommandeResponse assignerFournisseur(Long id, Long fournisseurId);

    BonDeCommandeResponse annuler(Long id);

    byte[] generatePdf(Long id);

    void delete(Long id);
}
