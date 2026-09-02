package sn.oas.facturation.features.recu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.recu.data.entity.Recu;
import sn.oas.facturation.features.recu.dto.RecuRequest;
import sn.oas.facturation.features.recu.repository.RecuRepository;
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

    @Override
    @Transactional
    public Recu create(RecuRequest request) {
        Facture facture = factureRepository.findById(request.getFactureId())
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable avec l'id : " + request.getFactureId()));

        if (facture.getStatutPaiement() == StatutPaiement.PAYE) {
            throw new BadRequestException("Cette facture est déjà totalement payée");
        }

        if (request.getMontant() == null || request.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le montant doit être supérieur à zéro");
        }

        if (request.getMontant().compareTo(facture.getResteAPayer()) > 0) {
            throw new BadRequestException("Le montant du reçu dépasse le reste à payer (" + facture.getResteAPayer() + ")");
        }

        String numero = documentNumberGeneratorService.generateNextNumber(DocumentType.RC);

        Recu recu = Recu.builder()
                .numero(numero)
                .facture(facture)
                .montant(request.getMontant())
                .modePaiement(request.getModePaiement())
                .remarque(request.getRemarque())
                .build();

        recu = recuRepository.save(recu);

        // Update Facture
        facture.setMontantPaye(facture.getMontantPaye().add(request.getMontant()));
        facture.setResteAPayer(facture.getMontantTotal().subtract(facture.getMontantPaye()));

        if (facture.getResteAPayer().compareTo(BigDecimal.ZERO) <= 0) {
            facture.setStatutPaiement(StatutPaiement.PAYE);

            // Advance Fiche Atelier to TERMINE if it was waiting for payment
            if (facture.getOrdreReparation() != null) {
                OrdreReparation fiche = facture.getOrdreReparation();
                if (fiche.getStatut() == StatutOrdreReparation.EN_ATTENTE_PAIEMENT) {
                    fiche.setStatut(StatutOrdreReparation.TERMINE);
                    ordreReparationRepository.save(fiche);
                }
            }
        } else {
            facture.setStatutPaiement(StatutPaiement.PARTIEL);
        }

        factureRepository.save(facture);

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
