package sn.oas.facturation.features.client.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.client.repository.ClientRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Règles centralisées. Les agents peuvent poursuivre une vente, mais voient les alertes. */
@Service @RequiredArgsConstructor
public class PolitiqueFinanciereClientService {
    private final FactureRepository factureRepository;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<String> avertissementsProforma(Client client, BigDecimal montantProforma) {
        if (client == null || client.getId() == null) return List.of();
        List<String> avertissements = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        boolean retard = factureRepository.existsFactureImpayeeEnRetard(client.getId(), now)
                || (client.getEcheance() != null && client.getEcheance() > 0
                && factureRepository.existsFactureLegacyImpayeeEnRetard(client.getId(), now.minusDays(client.getEcheance())));
        if (retard) avertissements.add("Attention : ce client possède au moins une facture échue et impayée.");
        if (client.getEcheance() != null && client.getEcheance() > 0 && client.getMontantPlafondEcheance() != null) {
            BigDecimal periode = zero(factureRepository.sumMontantFacturesDepuis(client.getId(), now.minusDays(client.getEcheance())));
            BigDecimal apres = periode.add(zero(montantProforma));
            if (apres.compareTo(client.getMontantPlafondEcheance()) >= 0)
                avertissements.add("Attention : le plafond de facturation sur la période d'échéance sera atteint ou dépassé (" + apres.toPlainString() + " / " + client.getMontantPlafondEcheance().toPlainString() + " F CFA). La création du proforma reste autorisée pour l'agent.");
        }
        return avertissements;
    }

    @Transactional(readOnly = true)
    public String motifBlocageReservation(Long clientId) {
        Client client = clientRepository.findById(clientId).orElse(null);
        if (client == null) return null;
        return evaluerReservation(client).motifBlocage();
    }

    @Transactional(readOnly = true)
    public EvaluationReservation evaluerReservation(Long clientId) {
        Client client = clientRepository.findById(clientId).orElse(null);
        return client == null ? EvaluationReservation.autorisee() : evaluerReservation(client);
    }

    @Transactional(readOnly = true)
    public String motifBlocageRetard(Long clientId) {
        Client client = clientRepository.findById(clientId).orElse(null);
        if (client == null) return null;
        LocalDateTime now = LocalDateTime.now();
        boolean retard = factureRepository.existsFactureImpayeeEnRetard(clientId, now)
                || (client.getEcheance() != null && client.getEcheance() > 0
                && factureRepository.existsFactureLegacyImpayeeEnRetard(clientId, now.minusDays(client.getEcheance())));
        return retard ? "Votre compte comporte une facture échue impayée. Contactez OAS pour régulariser votre situation avant de prendre un rendez-vous." : null;
    }

    public EvaluationReservation evaluerReservation(Client client) {
        Long clientId = client.getId();
        LocalDateTime now = LocalDateTime.now();
        boolean retard = factureRepository.existsFactureImpayeeEnRetard(clientId, now)
                || (client.getEcheance() != null && client.getEcheance() > 0
                && factureRepository.existsFactureLegacyImpayeeEnRetard(clientId, now.minusDays(client.getEcheance())));
        if (retard) {
            return new EvaluationReservation(false, false, true,
                    "Votre compte comporte une facture échue impayée. Contactez OAS pour régulariser votre situation avant de prendre un rendez-vous.",
                    client.getMontantPlafondEcheance(), BigDecimal.ZERO, client.getEcheance());
        }

        BigDecimal plafond = client.getMontantPlafondEcheance();
        Integer echeance = client.getEcheance();
        if (plafond == null || echeance == null || echeance <= 0) {
            return EvaluationReservation.autorisee();
        }

        BigDecimal montantConsomme = zero(factureRepository.sumMontantFacturesDepuis(
                clientId, now.minusDays(echeance)));
        boolean plafondAtteint = montantConsomme.compareTo(plafond) >= 0;
        String motif = plafondAtteint
                ? "Le plafond de facturation prévu sur votre période d'échéance est atteint. Une demande de dérogation exceptionnelle peut être soumise à OAS."
                : null;
        return new EvaluationReservation(!plafondAtteint, plafondAtteint, false, motif,
                plafond, montantConsomme, echeance);
    }

    public record EvaluationReservation(
            boolean workflowAutorise,
            boolean plafondAtteint,
            boolean factureEchueImpayee,
            String motifBlocage,
            BigDecimal plafond,
            BigDecimal montantConsomme,
            Integer echeanceJours) {
        private static EvaluationReservation autorisee() {
            return new EvaluationReservation(true, false, false, null, null, BigDecimal.ZERO, null);
        }

        public boolean derogationPossible() {
            return plafondAtteint && !factureEchueImpayee;
        }
    }

    private static BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
}
