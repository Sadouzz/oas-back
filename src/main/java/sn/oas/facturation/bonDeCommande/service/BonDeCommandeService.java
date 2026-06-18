package sn.oas.facturation.bonDeCommande.service;

import sn.oas.facturation.bonDeCommande.dto.BonDeCommandeCreateRequest;
import sn.oas.facturation.bonDeCommande.dto.BonDeCommandeResponse;
import sn.oas.facturation.bonDeCommande.dto.BonDeCommandeUpdateRequest;
import sn.oas.facturation.bonDeCommande.dto.BonDeLivraisonRequest;

import java.util.List;

public interface BonDeCommandeService {

    BonDeCommandeResponse create(BonDeCommandeCreateRequest request);

    BonDeCommandeResponse update(
            Long id,
            BonDeCommandeUpdateRequest request
    );

    BonDeCommandeResponse getById(Long id);

    List<BonDeCommandeResponse> getAll();

    List<BonDeCommandeResponse> search(String keyword);

    List<BonDeCommandeResponse> getRecentBonDeCommandes();

    BonDeCommandeResponse envoyer(Long id);

    BonDeCommandeResponse receptionner(Long id);

    BonDeCommandeResponse receptionnerAvecQuantites(Long id, BonDeLivraisonRequest request);

    BonDeCommandeResponse assignerFournisseur(Long id, Long fournisseurId);

    BonDeCommandeResponse annuler(Long id);

    byte[] generatePdf(Long id);

    void delete(Long id);
}