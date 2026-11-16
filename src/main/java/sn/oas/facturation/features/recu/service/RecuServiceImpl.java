package sn.oas.facturation.features.recu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.service.PortefeuilleClientService;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.recu.data.entity.Recu;
import sn.oas.facturation.features.recu.dto.RecuRequest;
import sn.oas.facturation.features.recu.repository.RecuRepository;
import sn.oas.facturation.features.notification.service.AgentNotificationService;
import sn.oas.facturation.features.notification.service.NotificationService;
import sn.oas.facturation.features.user.data.enums.Role;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.documentNumber.DocumentType;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecuServiceImpl implements RecuService {

    private final RecuRepository recuRepository;
    private final FactureRepository factureRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final DocumentNumberGeneratorService documentNumberGeneratorService;
    private final NotificationService notificationService;
    private final AgentNotificationService agentNotificationService;
    private final PortefeuilleClientService portefeuilleClientService;

    @Override
    @Transactional
    public Recu create(RecuRequest request) {
        Facture facture = factureRepository.findByIdForPaiement(request.getFactureId())
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable avec l'id : " + request.getFactureId()));

        return enregistrerPaiement(facture, request);
    }

    @Override
    @Transactional
    public Recu createPaiementIntegral(Long factureId, String modePaiement) {
        Facture facture = factureRepository.findByIdForPaiement(factureId)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable avec l'id : " + factureId));

        BigDecimal montantPaye = facture.getMontantPaye() == null ? BigDecimal.ZERO : facture.getMontantPaye();
        if (facture.getMontantTotal() == null) {
            throw new BadRequestException("Le montant total de la facture est introuvable");
        }
        BigDecimal montantRestant = facture.getMontantTotal().subtract(montantPaye);
        if (montantRestant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Cette facture est déjà totalement payée");
        }
        // Le total et les paiements déjà enregistrés sont la référence pour ce règlement.
        facture.setResteAPayer(montantRestant);

        RecuRequest request = RecuRequest.builder()
                .factureId(factureId)
                .montant(montantRestant)
                .modePaiement(modePaiement)
                .build();
        return enregistrerPaiement(facture, request);
    }

    private Recu enregistrerPaiement(Facture facture, RecuRequest request) {
        if (facture.getStatutPaiement() == StatutPaiement.PAYE) {
            throw new BadRequestException("Cette facture est déjà totalement payée");
        }

        BigDecimal montantPayeActuel = facture.getMontantPaye() == null ? BigDecimal.ZERO : facture.getMontantPaye();
        BigDecimal resteAPayer = facture.getResteAPayer();
        if (resteAPayer == null && facture.getMontantTotal() != null) {
            resteAPayer = facture.getMontantTotal().subtract(montantPayeActuel);
            facture.setResteAPayer(resteAPayer);
        }
        if (resteAPayer == null) {
            throw new BadRequestException("Le reste à payer de la facture est introuvable");
        }

        if (request.getMontant() == null || request.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le montant doit être supérieur à zéro");
        }

        if (request.getMontant().compareTo(resteAPayer) > 0) {
            throw new BadRequestException("Le montant du reçu dépasse le reste à payer (" + resteAPayer + ")");
        }

        String numero = documentNumberGeneratorService.generateNextNumber(DocumentType.RC);

        boolean paiementParSolde = PortefeuilleClientService.MODE_PAIEMENT.equalsIgnoreCase(
                request.getModePaiement() == null ? "" : request.getModePaiement().trim());
        String modePaiement = request.getModePaiement() == null || request.getModePaiement().isBlank()
                ? "ESPECE" : request.getModePaiement().trim().toUpperCase(java.util.Locale.ROOT);
        if (paiementParSolde && request.getMontant().scale() > 2) {
            throw new BadRequestException("Le montant du paiement doit comporter au maximum deux décimales.");
        }

        // Le régime fiscal est figé sur la facture : un timbre HT est calculé à son émission.
        // Un encaissement ne doit jamais recalculer ni ajouter un second timbre, quel que soit son mode.
        BigDecimal montantTimbre = BigDecimal.ZERO;

        Recu recu = Recu.builder()
                .numero(numero)
                .facture(facture)
                .montant(request.getMontant())
                .montantTimbre(montantTimbre)
                .modePaiement(paiementParSolde ? PortefeuilleClientService.MODE_PAIEMENT : modePaiement)
                .remarque(request.getRemarque())
                .build();

        recu = recuRepository.save(recu);
        if (paiementParSolde) {
            portefeuilleClientService.debiterPourFacture(facture, recu, request.getMontant());
        }

        // Update Facture
        facture.setMontantPaye(montantPayeActuel.add(request.getMontant()));
        facture.setResteAPayer(facture.getMontantTotal().subtract(facture.getMontantPaye()));
        boolean vehicleReadyForDelivery = false;

        if (facture.getResteAPayer().compareTo(BigDecimal.ZERO) <= 0) {
            facture.setStatutPaiement(StatutPaiement.PAYE);

            // Advance Fiche Atelier to TERMINE if it was waiting for payment
            if (facture.getOrdreReparation() != null) {
                OrdreReparation fiche = facture.getOrdreReparation();
                if (fiche.getStatut() == StatutOrdreReparation.BL || fiche.getStatut() == StatutOrdreReparation.PAIEMENT || fiche.getStatut() == StatutOrdreReparation.FACTURATION || fiche.getStatut() == StatutOrdreReparation.PRET_A_LIVRER) {
                    fiche.setStatut(StatutOrdreReparation.FACTURATION);
                    ordreReparationRepository.save(fiche);
                    vehicleReadyForDelivery = true;
                    if (facture.getClient() != null) {
                        notificationService.sendInAppNotification(facture.getClient(), "Véhicule prêt à récupérer",
                                "Le paiement de votre facture est complet. Votre véhicule est prêt à être récupéré.");
                    }
                }
            }
        } else {
            facture.setStatutPaiement(StatutPaiement.PARTIEL);
        }

        factureRepository.save(facture);

        String statutLibelle = facture.getStatutPaiement() == StatutPaiement.PAYE ? "payée intégralement" : "partiellement payée";
        notificationService.sendNotification(facture.getClient(), "Paiement enregistré",
                "Le reçu " + recu.getNumero() + " de " + request.getMontant()
                        + " a été enregistré. La facture " + facture.getNumero() + " est " + statutLibelle + ".");
        agentNotificationService.notifyRoleInGarage(Role.AGENT, facture.getGarage(),
                facture.getStatutPaiement() == StatutPaiement.PAYE ? "Facture payée" : "Paiement partiel reçu",
                "Le reçu " + recu.getNumero() + " a été enregistré pour la facture " + facture.getNumero() + ".");
        if (vehicleReadyForDelivery) {
            agentNotificationService.notifyRoleInGarage(Role.CHEF_ATELIER, facture.getGarage(),
                    "Véhicule prêt à livrer", "La facture " + facture.getNumero()
                            + " est soldée. Le véhicule associé peut être remis au client.");
        }

        return recu;
    }

    @Override
    public List<Recu> getByFacture(Long factureId) {
        return recuRepository.findByFactureIdOrderByDatePaiementDesc(factureId);
    }

    @Override
    public List<Recu> getClientRecus(Client client) {
        return recuRepository.findByFactureClientIdOrderByDatePaiementDesc(client.getId());
    }

    @Override
    public List<Recu> getAll() {
        return recuRepository.findAll();
    }
}
