package sn.oas.facturation.features.client.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.client.repository.ClientRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Règles centralisées. Les agents peuvent poursuivre une vente, mais voient les alertes. */
@Service @RequiredArgsConstructor
public class PolitiqueFinanciereClientService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final FactureRepository factureRepository;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<String> avertissementsProforma(Client client, BigDecimal montantProforma) {
        if (client == null || client.getId() == null) return List.of();
        List<String> avertissements = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        List<Facture> enRetard = facturesEnRetard(client, now);
        if (!enRetard.isEmpty()) {
            avertissements.add("Attention : " + enRetard.size()
                    + " facture(s) impayée(s) ont dépassé leur échéance. La création du proforma reste autorisée pour l'agent.");
        }
        BigDecimal plafond = client.getMontantPlafondEcheance();
        if (plafond != null) {
            BigDecimal encoursApres = zero(factureRepository.sumResteAPayerByClientId(client.getId()))
                    .add(zero(montantProforma));
            if (encoursApres.compareTo(plafond) >= 0) {
                avertissements.add("Attention : l'encours impayé atteindrait ou dépasserait le plafond après cette opération ("
                        + encoursApres.toPlainString() + " / " + plafond.toPlainString()
                        + " F CFA). La création du proforma reste autorisée pour l'agent.");
            }
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

    public EvaluationReservation evaluerReservation(Client client) {
        Long clientId = client.getId();
        LocalDateTime now = LocalDateTime.now();
        BigDecimal encoursImpayes = zero(factureRepository.sumResteAPayerByClientId(clientId));
        BigDecimal plafond = client.getMontantPlafondEcheance();
        Integer echeance = client.getEcheance();
        boolean plafondAtteint = plafond != null && encoursImpayes.compareTo(plafond) >= 0;
        List<Facture> facturesEnRetard = facturesEnRetard(client, now);
        boolean factureEchueImpayee = !facturesEnRetard.isEmpty();
        String motif = construireMotifBlocage(plafondAtteint, factureEchueImpayee,
                encoursImpayes, plafond, facturesEnRetard, echeance);
        return new EvaluationReservation(!plafondAtteint && !factureEchueImpayee,
                plafondAtteint, factureEchueImpayee, motif, plafond, encoursImpayes,
                echeance, facturesEnRetard.stream().map(f -> new FactureEchueImpayee(
                        f.getId(), f.getNumero(), f.getDateCreation(), effectiveDueDate(f, echeance),
                        montantRestant(f))).toList());
    }

    public record EvaluationReservation(
            boolean workflowAutorise,
            boolean plafondAtteint,
            boolean factureEchueImpayee,
            String motifBlocage,
            BigDecimal plafond,
            BigDecimal encoursImpayes,
            Integer echeanceJours,
            List<FactureEchueImpayee> facturesEchuesImpayees) {
        public EvaluationReservation(boolean workflowAutorise, boolean plafondAtteint,
                boolean factureEchueImpayee, String motifBlocage, BigDecimal plafond,
                BigDecimal encoursImpayes, Integer echeanceJours) {
            this(workflowAutorise, plafondAtteint, factureEchueImpayee, motifBlocage,
                    plafond, encoursImpayes, echeanceJours, List.of());
        }

        private static EvaluationReservation autorisee() {
            return new EvaluationReservation(true, false, false, null, null, BigDecimal.ZERO, null, List.of());
        }

        public boolean derogationPossible() {
            return !workflowAutorise;
        }
    }

    public record FactureEchueImpayee(
            Long factureId,
            String numero,
            LocalDateTime dateEmission,
            LocalDateTime dateEcheance,
            BigDecimal resteAPayer) {}

    private List<Facture> facturesEnRetard(Client client, LocalDateTime maintenant) {
        LocalDateTime limiteAnciennesFactures = client.getEcheance() != null && client.getEcheance() > 0
                ? maintenant.minusDays(client.getEcheance()) : null;
        return factureRepository.findFacturesImpayeesEnRetard(client.getId(), maintenant, limiteAnciennesFactures);
    }

    private String construireMotifBlocage(boolean plafondAtteint, boolean factureEchueImpayee,
            BigDecimal encoursImpayes, BigDecimal plafond, List<Facture> facturesEchues, Integer echeance) {
        if (!plafondAtteint && !factureEchueImpayee) return null;
        List<String> motifs = new ArrayList<>();
        if (plafondAtteint) {
            motifs.add("L'encours impayé de votre compte atteint ou dépasse le plafond : "
                    + encoursImpayes.toPlainString() + " / " + plafond.toPlainString() + " F CFA.");
        }
        if (factureEchueImpayee) motifs.add(decrireFacturesEnRetard(facturesEchues, echeance));
        motifs.add("Une nouvelle demande nécessite une dérogation exceptionnelle d'un Super Agent.");
        return String.join("\n", motifs);
    }

    private String decrireFacturesEnRetard(List<Facture> factures, Integer echeance) {
        StringBuilder details = new StringBuilder("Facture(s) impayée(s) ayant dépassé leur échéance (")
                .append(factures.size()).append(") :");
        for (Facture facture : factures) {
            details.append("\n• ").append(facture.getNumero())
                    .append(" — émise le ").append(formater(facture.getDateCreation()))
                    .append(", échéance le ").append(formater(effectiveDueDate(facture, echeance)))
                    .append(" — reste dû : ").append(montantRestant(facture).toPlainString()).append(" F CFA");
        }
        return details.toString();
    }

    private LocalDateTime effectiveDueDate(Facture facture, Integer echeance) {
        if (facture.getDateEcheance() != null) return facture.getDateEcheance();
        return facture.getDateCreation() != null && echeance != null && echeance > 0
                ? facture.getDateCreation().plusDays(echeance) : null;
    }

    private BigDecimal montantRestant(Facture facture) {
        if (facture.getResteAPayer() != null) return zero(facture.getResteAPayer());
        return zero(facture.getMontantTotal()).subtract(zero(facture.getMontantPaye())).max(BigDecimal.ZERO);
    }

    private String formater(LocalDateTime date) {
        return date == null ? "date inconnue" : DATE_FORMAT.format(date);
    }

    private static BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
}
