package sn.oas.facturation.features.marketplace.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.marketplace.data.entity.DemandeProduit;
import sn.oas.facturation.features.marketplace.data.entity.Produit;
import sn.oas.facturation.features.marketplace.data.enums.StatutDemandeProduit;
import sn.oas.facturation.features.marketplace.dto.DemandeProduitRequest;
import sn.oas.facturation.features.marketplace.repository.DemandeProduitRepository;
import sn.oas.facturation.features.marketplace.repository.ProduitRepository;
import sn.oas.facturation.features.notification.service.AgentNotificationService;
import sn.oas.facturation.features.notification.service.NotificationService;
import sn.oas.facturation.features.user.data.enums.Role;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DemandeProduitServiceImpl implements DemandeProduitService {

    private final DemandeProduitRepository demandeProduitRepository;
    private final ProduitRepository produitRepository;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;
    private final AgentNotificationService agentNotificationService;
    private final NotificationService notificationService;

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

        DemandeProduit saved = demandeProduitRepository.save(demande);
        String message = "Nouvelle demande " + saved.getNumero() + " pour le produit " + produit.getNom() + ".";
        agentNotificationService.notifyRole(Role.AGENT, "Nouvelle demande Marketplace", message);
        agentNotificationService.notifyRole(Role.SUPER_AGENT, "Nouvelle demande Marketplace", message);
        agentNotificationService.notifyRole(Role.MASTER, "Nouvelle demande Marketplace", message);
        return saved;
    }

    @Override
    @Transactional
    public DemandeProduit updateStatus(Long id, String statut) {
        DemandeProduit demande = getById(id);
        StatutDemandeProduit nouveauStatut = StatutDemandeProduit.valueOf(statut.toUpperCase());
        StatutDemandeProduit ancienStatut = demande.getStatut();
        demande.setStatut(nouveauStatut);
        DemandeProduit saved = demandeProduitRepository.save(demande);
        if (nouveauStatut != ancienStatut) {
            notificationService.sendNotification(demande.getClient(), titrePour(nouveauStatut),
                    "Le statut de votre demande " + demande.getNumero() + " pour " + demande.getProduit().getNom()
                            + " est maintenant : " + labelPour(nouveauStatut) + ".");
        }
        return saved;
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
        DemandeProduit saved = demandeProduitRepository.save(demande);
        String message = "Le client a annulé la demande " + demande.getNumero() + " pour " + demande.getProduit().getNom() + ".";
        agentNotificationService.notifyRole(Role.AGENT, "Demande Marketplace annulée", message);
        agentNotificationService.notifyRole(Role.SUPER_AGENT, "Demande Marketplace annulée", message);
        agentNotificationService.notifyRole(Role.MASTER, "Demande Marketplace annulée", message);
        return saved;
    }

    private String titrePour(StatutDemandeProduit statut) {
        return switch (statut) {
            case ACCEPTEE -> "Demande Marketplace acceptée";
            case REFUSEE -> "Demande Marketplace refusée";
            case COMMANDEE -> "Produit commandé";
            case ANNULEE -> "Demande Marketplace clôturée";
            case LIVREE -> "Produit livré";
            case EN_ATTENTE -> "Demande Marketplace en attente";
        };
    }

    private String labelPour(StatutDemandeProduit statut) {
        return switch (statut) {
            case ACCEPTEE -> "acceptée";
            case REFUSEE -> "refusée";
            case COMMANDEE -> "commandée";
            case ANNULEE -> "annulée";
            case LIVREE -> "livrée";
            case EN_ATTENTE -> "en attente";
        };
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
