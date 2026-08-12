package sn.oas.facturation.marketplace.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.marketplace.data.entity.DemandeProduit;
import sn.oas.facturation.marketplace.data.entity.Produit;
import sn.oas.facturation.marketplace.data.enums.StatutDemandeProduit;
import sn.oas.facturation.marketplace.dto.DemandeProduitRequest;
import sn.oas.facturation.marketplace.repository.DemandeProduitRepository;
import sn.oas.facturation.marketplace.repository.ProduitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DemandeProduitServiceImpl implements DemandeProduitService {

    private final DemandeProduitRepository demandeProduitRepository;
    private final ProduitRepository produitRepository;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;

    @Override
    @Transactional
    public DemandeProduit create(Client client, DemandeProduitRequest request) {
        Produit produit = produitRepository.findById(request.produitId())
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));

        DemandeProduit demande = DemandeProduit.builder()
                .numero(documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.DMD))
                .client(client)
                .produit(produit)
                .quantite(request.quantite() != null ? request.quantite() : 1)
                .message(request.message())
                .statut(StatutDemandeProduit.EN_ATTENTE)
                .dateCreation(java.time.LocalDateTime.now())
                .build();

        return demandeProduitRepository.save(demande);
    }

    @Override
    @Transactional
    public DemandeProduit updateStatus(Long id, String statut) {
        DemandeProduit demande = getById(id);
        demande.setStatut(StatutDemandeProduit.valueOf(statut.toUpperCase()));
        return demandeProduitRepository.save(demande);
    }

    @Override
    @Transactional(readOnly = true)
    public DemandeProduit getById(Long id) {
        return demandeProduitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
    }

    @Override
    @Transactional(readOnly = true)
    public DemandeProduit getByIdAndClient(Long id, Client client) {
        return demandeProduitRepository.findByIdAndClient(id, client)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
    }

    @Override
    @Transactional
    public DemandeProduit cancel(Long id, Client client) {
        DemandeProduit demande = getByIdAndClient(id, client);
        if (!StatutDemandeProduit.EN_ATTENTE.equals(demande.getStatut())) {
            throw new IllegalStateException("Seules les demandes en attente peuvent être annulées");
        }
        demande.setStatut(StatutDemandeProduit.ANNULEE);
        return demandeProduitRepository.save(demande);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeProduit> getByClient(Client client) {
        return demandeProduitRepository.findByClientOrderByDateCreationDesc(client);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeProduit> getAll() {
        return demandeProduitRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeProduit> getByProduit(Long produitId) {
        return demandeProduitRepository.findByProduitIdOrderByDateCreationDesc(produitId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeProduit> getHistoriqueByClient(Client client) {
        return demandeProduitRepository.findByClientOrderByDateCreationDesc(client);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeProduit> getHistoriqueTous() {
        return demandeProduitRepository.findAllByOrderByDateCreationDesc();
    }
}
