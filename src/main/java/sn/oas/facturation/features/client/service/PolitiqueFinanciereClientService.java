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
        BigDecimal encours = zero(factureRepository.sumResteAPayerByClientId(client.getId()));
        BigDecimal totalApresCreation = encours.add(zero(montantProforma));
        BigDecimal plafond = client.getMontantPlafond() == null ? null : BigDecimal.valueOf(client.getMontantPlafond());
        List<String> avertissements = new ArrayList<>();
        if (plafond != null && totalApresCreation.compareTo(plafond) >= 0) {
            avertissements.add("Attention : l'encours impayé atteindra ou dépassera le plafond autorisé (" + totalApresCreation.toPlainString() + " / " + plafond.toPlainString() + " F CFA). La création du proforma reste autorisée pour l'agent.");
        }
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
        LocalDateTime now = LocalDateTime.now();
        boolean retard = factureRepository.existsFactureImpayeeEnRetard(clientId, now)
                || (client.getEcheance() != null && client.getEcheance() > 0
                && factureRepository.existsFactureLegacyImpayeeEnRetard(clientId, now.minusDays(client.getEcheance())));
        if (retard) return "Votre compte comporte une facture échue impayée. Contactez OAS pour régulariser votre situation avant de prendre un rendez-vous.";
        if (client.getMontantPlafond() != null) {
            BigDecimal encours = zero(factureRepository.sumResteAPayerByClientId(clientId));
            if (encours.compareTo(BigDecimal.valueOf(client.getMontantPlafond())) > 0)
                return "Votre encours impayé dépasse le plafond autorisé. Contactez OAS avant de prendre un rendez-vous.";
        }
        if (client.getEcheance() != null && client.getEcheance() > 0 && client.getMontantPlafondEcheance() != null) {
            BigDecimal periode = zero(factureRepository.sumMontantFacturesDepuis(clientId, now.minusDays(client.getEcheance())));
            if (periode.compareTo(client.getMontantPlafondEcheance()) >= 0)
                return "Le plafond de facturation prévu sur votre période d'échéance est atteint. Contactez OAS avant de prendre un rendez-vous.";
        }
        return null;
    }

    private static BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
}
