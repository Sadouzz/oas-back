package sn.oas.facturation.features.bonDeCommande.service;

import org.springframework.data.domain.Page;
import sn.oas.facturation.features.bonDeCommande.data.entity.BonDeCommande;
import sn.oas.facturation.features.bonDeCommande.dto.BonDeCommandeCreateRequest;
import sn.oas.facturation.features.bonDeCommande.dto.BonDeCommandeUpdateRequest;
import sn.oas.facturation.features.bonDeCommande.dto.ReceptionBonDeCommandeRequest;

import java.util.List;

public interface BonDeCommandeService {

    BonDeCommande create(BonDeCommandeCreateRequest request);

    BonDeCommande update(
            Long id,
            BonDeCommandeUpdateRequest request
    );

    BonDeCommande getById(Long id);

    Page<BonDeCommande> getAll(int page, int size);

    List<BonDeCommande> getAll();

    List<BonDeCommande> search(String keyword);
    Page<BonDeCommande> search(String keyword, int page, int size);

    List<BonDeCommande> getRecentBonDeCommandes();

    BonDeCommande envoyer(Long id);

    BonDeCommande receptionner(Long id);

    BonDeCommande receptionnerAvecQuantites(Long id, ReceptionBonDeCommandeRequest request);

    BonDeCommande assignerFournisseur(Long id, Long fournisseurId);

    BonDeCommande annuler(Long id);

    byte[] generatePdf(Long id);

    void delete(Long id);
}
