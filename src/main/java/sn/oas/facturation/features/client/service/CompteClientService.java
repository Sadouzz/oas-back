package sn.oas.facturation.features.client.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.client.data.entity.*;
import sn.oas.facturation.features.client.data.enums.TypeClient;
import sn.oas.facturation.features.client.dto.*;
import sn.oas.facturation.features.client.repository.*;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.rendezvous.data.enums.StatutDemandeDerogationPlafond;
import sn.oas.facturation.features.rendezvous.repository.DemandeDerogationPlafondRepository;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.Comparator;

@Service @RequiredArgsConstructor
public class CompteClientService {
    private static final Pattern NINEA_PATTERN = Pattern.compile("(?i)^[A-Z0-9]{3,40}$");
    private final ClientRepository clientRepository;
    private final CompteClientRepository compteRepository;
    private final FactureRepository factureRepository;
    private final PolitiqueFinanciereClientService politiqueFinanciereClientService;
    private final DemandeDerogationPlafondRepository demandeDerogationPlafondRepository;

    @Transactional
    public CompteClientResponse configurer(Long clientId, CompteClientRequest request) {
        if (request == null) throw new IllegalArgumentException("La configuration du compte est obligatoire.");
        if (request.echeanceJours() == null || request.echeanceJours() < 1 || request.echeanceJours() > 3650)
            throw new IllegalArgumentException("L'échéance doit être comprise entre 1 et 3650 jours.");
        if (request.plafondPeriode() == null || request.plafondPeriode().signum() <= 0)
            throw new IllegalArgumentException("Le plafond d'encours impayé doit être supérieur à zéro.");
        if (request.raisonSociale() == null || request.raisonSociale().isBlank())
            throw new IllegalArgumentException("La raison sociale est obligatoire.");
        if (request.ninea() == null || request.ninea().isBlank())
            throw new IllegalArgumentException("Le NINEA est obligatoire.");
        Client client = clientRepository.findById(clientId).orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));
        String ninea = request.ninea().trim().toUpperCase(Locale.ROOT);
        if (!NINEA_PATTERN.matcher(ninea).matches()) throw new IllegalArgumentException("Format de NINEA invalide.");
        if (request.rccm() == null || request.rccm().isBlank()) throw new IllegalArgumentException("Le RCCM est obligatoire.");
        if (request.rib() == null || request.rib().isBlank()) throw new IllegalArgumentException("Le RIB est obligatoire.");
        if (request.actif() == null) throw new IllegalArgumentException("Le statut du compte est obligatoire.");
        if (clientRepository.existsByNumeroEntrepriseIgnoreCaseAndIdNot(ninea, clientId))
            throw new IllegalArgumentException("Ce NINEA est déjà associé à un autre client.");

        // L'ouverture du compte établit l'identité d'entreprise, sans créer de nouvel utilisateur.
        client.setTypeClient(TypeClient.ENTREPRISE);
        client.setRaisonSociale(request.raisonSociale().trim());
        client.setNumeroEntreprise(ninea);
        if (client.getEmailEntreprise() == null) client.setEmailEntreprise(client.getEmail());
        if (client.getAdresseEntreprise() == null) client.setAdresseEntreprise(client.getAdresse());
        client.setEcheance(request.echeanceJours());
        client.setMontantPlafondEcheance(request.plafondPeriode());
        client.setRccm(request.rccm().trim());
        client.setRib(request.rib().trim());
        client.setClientFidele(true);
        client.setUpdatedAt(LocalDateTime.now());

        CompteClient compte = compteRepository.findByClientId(clientId).orElseGet(() -> CompteClient.builder()
                .client(client).soldeCredit(BigDecimal.ZERO).actif(true).build());
        compte.setActif(request.actif());
        client.setClientFidele(compte.isActif());
        clientRepository.save(client);
        compteRepository.save(compte);
        return lire(clientId);
    }

    @Transactional(readOnly = true)
    public CompteClientResponse lire(Long clientId) {
        Client client = clientRepository.findById(clientId).orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));
        CompteClient compte = compteRepository.findByClientId(clientId).orElse(null);
        var evaluation = politiqueFinanciereClientService.evaluerReservation(client);
        BigDecimal encours = evaluation.encoursImpayes();
        LocalDateTime now = LocalDateTime.now();
        Integer jours = client.getEcheance();
        LocalDateTime depuis = jours == null || jours <= 0 ? now.minusYears(100) : now.minusDays(jours);
        BigDecimal periode = factureRepository.sumMontantFacturesDepuis(clientId, depuis);
        if (periode == null) periode = BigDecimal.ZERO;
        boolean demandeEnAttente = demandeDerogationPlafondRepository
                .findFirstByClientIdAndStatutOrderByDateDemandeDesc(clientId, StatutDemandeDerogationPlafond.EN_ATTENTE)
                .isPresent();
        String motif = demandeEnAttente
                ? "Une demande de dérogation est en attente de décision par un Super Agent."
                : evaluation.motifBlocage();
        return new CompteClientResponse(clientId, client.getTypeClient() == null ? TypeClient.PARTICULIER.name() : client.getTypeClient().name(),
                client.getRaisonSociale(), client.getNumeroEntreprise(), compte != null, compte != null && compte.isActif(), jours,
                client.getMontantPlafondEcheance(), encours, periode, evaluation.factureEchueImpayee(),
                evaluation.workflowAutorise() && !demandeEnAttente, motif,
                client.getRccm(), client.getRib(), compte == null ? null : compte.getDateCreation());
    }

    @Transactional(readOnly = true)
    public SyntheseFinanciereClientResponse lireSynthese(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));
        var evaluation = politiqueFinanciereClientService.evaluerReservation(client);
        LocalDateTime maintenant = LocalDateTime.now();
        Integer echeance = client.getEcheance();
        LocalDateTime debutEmission = echeance != null && echeance > 0
                ? maintenant.minusDays(echeance) : maintenant;
        LocalDateTime finEmission = echeance != null && echeance > 0
                ? maintenant.plusDays(7).minusDays(echeance) : maintenant;
        List<FactureEcheanceClientResponse> prochainesEcheances = factureRepository.findProchainesEcheancesImpayees(
                clientId, maintenant, maintenant.plusDays(7), echeance,
                debutEmission, finEmission)
                .stream().map(projection -> {
                    LocalDateTime dateEcheance = projection.getDateEcheance() != null
                            ? projection.getDateEcheance()
                            : projection.getDateEmission() == null || echeance == null || echeance <= 0 ? null
                                    : projection.getDateEmission().plusDays(echeance);
                    return new FactureEcheanceClientResponse(projection.getFactureId(), projection.getNumero(),
                            projection.getDateEmission(), dateEcheance, projection.getResteAPayer(),
                            projection.getOrdreReparationId(), projection.getNumeroOrdreReparation(),
                            projection.getImmatriculation(), projection.getMarque(), projection.getModele());
                }).sorted(Comparator.comparing(FactureEcheanceClientResponse::dateEcheance,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(5).toList();
        List<FactureEcheanceClientResponse> facturesBloquantes = evaluation.facturesEchuesImpayees().stream()
                .map(f -> new FactureEcheanceClientResponse(f.factureId(), f.numero(), f.dateEmission(),
                        f.dateEcheance(), f.resteAPayer(), null, null, null, null, null))
                .toList();
        BigDecimal disponible = evaluation.plafond() == null ? null
                : evaluation.plafond().subtract(evaluation.encoursImpayes()).max(BigDecimal.ZERO);
        boolean derogationEnAttente = demandeDerogationPlafondRepository
                .findFirstByClientIdAndStatutOrderByDateDemandeDesc(clientId, StatutDemandeDerogationPlafond.EN_ATTENTE)
                .isPresent();
        String motif = derogationEnAttente
                ? "Une demande de dérogation est en attente de décision par un Super Agent."
                : evaluation.motifBlocage();
        return new SyntheseFinanciereClientResponse(clientId, echeance, evaluation.plafond(),
                evaluation.encoursImpayes(), disponible, evaluation.plafond() != null,
                evaluation.plafondAtteint(), evaluation.factureEchueImpayee(), derogationEnAttente,
                evaluation.workflowAutorise() && !derogationEnAttente, motif,
                facturesBloquantes, prochainesEcheances);
    }

    @Transactional
    public void desactiver(Long clientId) {
        CompteClient compte = compteRepository.findByClientId(clientId).orElseThrow(() -> new ResourceNotFoundException("Compte client introuvable"));
        compte.setActif(false);
        Client client = compte.getClient();
        client.setClientFidele(false); // Les paramètres d'échéance et les données d'entreprise sont conservés.
        client.setUpdatedAt(LocalDateTime.now());
    }
}
