package sn.oas.facturation.recu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.facturation.data.enums.StatutFacturation;
import sn.oas.facturation.facture.data.entity.Facture;
import sn.oas.facturation.facture.repository.FactureRepository;
import sn.oas.facturation.notification.service.NotificationService;
import sn.oas.facturation.recu.data.entity.Recu;
import sn.oas.facturation.recu.dto.RecuResponse;
import sn.oas.facturation.recu.repository.RecuRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecuServiceImpl implements RecuService {

    private final RecuRepository recuRepository;
    private final FactureRepository factureRepository;
    private final NotificationService notificationService;

    @Transactional
    @Override
    public RecuResponse payerFacture(Long factureId, BigDecimal montant, String methodePaiement) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée"));

        if (facture.getStatut() == StatutFacturation.PAYEE) {
            throw new IllegalStateException("La facture est déjà payée");
        }

        BigDecimal dejaPaye = facture.getMontantPaye() != null ? facture.getMontantPaye() : BigDecimal.ZERO;
        BigDecimal resteAPayer = facture.getMontantTTC().subtract(dejaPaye);

        BigDecimal montantAPayer = montant;
        if (montantAPayer == null || montantAPayer.compareTo(BigDecimal.ZERO) <= 0) {
            montantAPayer = resteAPayer;
        }

        if (montantAPayer.compareTo(resteAPayer) > 0) {
            throw new IllegalArgumentException("Le montant du paiement dépasse le reste à payer (" + resteAPayer + ")");
        }

        BigDecimal nouveauMontantPaye = dejaPaye.add(montantAPayer);
        facture.setMontantPaye(nouveauMontantPaye);

        if (nouveauMontantPaye.compareTo(facture.getMontantTTC()) >= 0) {
            facture.setStatut(StatutFacturation.PAYEE);
        } else {
            facture.setStatut(StatutFacturation.PARTIELLEMENT_PAYEE);
        }
        factureRepository.save(facture);

        Recu recu = Recu.builder()
                .numero("REC-" + System.currentTimeMillis())
                .facture(facture)
                .montantPaye(montantAPayer)
                .datePaiement(LocalDateTime.now())
                .methodePaiement(methodePaiement)
                .build();

        recuRepository.save(recu);

        // Notify client
        String note = facture.getStatut() == StatutFacturation.PAYEE
                ? "Le paiement total de votre facture " + facture.getNumero() + " a été enregistré avec succès."
                : "Un paiement partiel de " + montantAPayer + " F CFA pour votre facture " + facture.getNumero() + " a été enregistré. Reste à payer : " + facture.getMontantTTC().subtract(nouveauMontantPaye) + " F CFA.";

        notificationService.sendNotification(facture.getClient(), "Paiement Enregistré", note);

        return RecuResponse.of(recu);
    }

    @Override
    public List<RecuResponse> getClientRecus(Client client) {
        return recuRepository.findByFactureClientIdOrderByDatePaiementDesc(client.getId())
                .stream()
                .map(RecuResponse::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecuResponse> getAllRecus() {
        return recuRepository.findAll()
                .stream()
                .map(RecuResponse::of)
                .collect(Collectors.toList());
    }
}
