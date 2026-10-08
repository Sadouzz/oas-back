package sn.oas.facturation.features.client.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.client.data.entity.*;
import sn.oas.facturation.features.client.data.enums.TypeClient;
import sn.oas.facturation.features.client.dto.*;
import sn.oas.facturation.features.client.repository.*;
import sn.oas.facturation.features.user.repository.UserRepository;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service @RequiredArgsConstructor
public class CompteClientService {
    private static final Pattern NINEA_PATTERN = Pattern.compile("(?i)^[A-Z0-9]{3,40}$");
    private final ClientRepository clientRepository;
    private final CompteClientRepository compteRepository;
    private final MouvementCreditClientRepository mouvementRepository;
    private final FactureRepository factureRepository;
    private final UserRepository userRepository;

    @Transactional
    public CompteClientResponse configurer(Long clientId, CompteClientRequest request) {
        if (request == null) throw new IllegalArgumentException("La configuration du compte est obligatoire.");
        if (request.remisePourcentage() == null || request.remisePourcentage() < 0 || request.remisePourcentage() > 100)
            throw new IllegalArgumentException("La remise doit être comprise entre 0 et 100 %.");
        if (request.echeanceJours() != null && (request.echeanceJours() < 1 || request.echeanceJours() > 3650))
            throw new IllegalArgumentException("L'échéance doit être comprise entre 1 et 3650 jours.");
        if (request.plafondPeriode() != null && request.plafondPeriode().signum() < 0)
            throw new IllegalArgumentException("Le plafond de période ne peut pas être négatif.");
        if (request.plafondPeriode() != null && (request.echeanceJours() == null || request.echeanceJours() <= 0))
            throw new IllegalArgumentException("Un délai de paiement est obligatoire pour définir un plafond de période.");
        if (request.raisonSociale() == null || request.raisonSociale().isBlank())
            throw new IllegalArgumentException("La raison sociale est obligatoire.");
        if (request.ninea() == null || request.ninea().isBlank())
            throw new IllegalArgumentException("Le NINEA est obligatoire.");
        Client client = clientRepository.findById(clientId).orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));
        String ninea = request.ninea().trim().toUpperCase(Locale.ROOT);
        if (!NINEA_PATTERN.matcher(ninea).matches()) throw new IllegalArgumentException("Format de NINEA invalide.");
        if (clientRepository.existsByNumeroEntrepriseIgnoreCaseAndIdNot(ninea, clientId))
            throw new IllegalArgumentException("Ce NINEA est déjà associé à un autre client.");

        // L'ouverture du compte établit l'identité d'entreprise, sans créer de nouvel utilisateur.
        client.setTypeClient(TypeClient.ENTREPRISE);
        client.setRaisonSociale(request.raisonSociale().trim());
        client.setNumeroEntreprise(ninea);
        if (client.getEmailEntreprise() == null) client.setEmailEntreprise(client.getEmail());
        if (client.getAdresseEntreprise() == null) client.setAdresseEntreprise(client.getAdresse());
        client.setMontantRemise(request.remisePourcentage());
        if (request.plafondCredit() != null && (request.plafondCredit().signum() < 0
                || request.plafondCredit().compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0
                || request.plafondCredit().stripTrailingZeros().scale() > 0))
            throw new IllegalArgumentException("Le plafond d'encours doit être un montant entier positif valide.");
        // PUT remplace les conditions envoyées : une valeur null efface la limite correspondante.
        client.setMontantPlafond(request.plafondCredit() == null ? null : request.plafondCredit().intValueExact());
        client.setEcheance(request.echeanceJours());
        client.setMontantPlafondEcheance(request.plafondPeriode());
        if (request.rccm() != null) client.setRccm(request.rccm().trim());
        if (request.rib() != null) client.setRib(request.rib().trim());
        client.setClientFidele(true);
        client.setUpdatedAt(LocalDateTime.now());

        CompteClient compte = compteRepository.findByClientId(clientId).orElseGet(() -> CompteClient.builder()
                .client(client).soldeCredit(BigDecimal.ZERO).actif(true).build());
        if (request.actif() != null) compte.setActif(request.actif());
        client.setClientFidele(compte.isActif());
        clientRepository.save(client);
        compteRepository.save(compte);
        return lire(clientId);
    }

    @Transactional(readOnly = true)
    public CompteClientResponse lire(Long clientId) {
        Client client = clientRepository.findById(clientId).orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));
        CompteClient compte = compteRepository.findByClientId(clientId).orElse(null);
        BigDecimal encours = factureRepository.sumResteAPayerByClientId(clientId);
        if (encours == null) encours = BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();
        Integer jours = client.getEcheance();
        LocalDateTime depuis = jours == null || jours <= 0 ? now.minusYears(100) : now.minusDays(jours);
        BigDecimal periode = factureRepository.sumMontantFacturesDepuis(clientId, depuis);
        if (periode == null) periode = BigDecimal.ZERO;
        boolean retard = factureRepository.existsFactureImpayeeEnRetard(clientId, now)
                || (jours != null && jours > 0 && factureRepository.existsFactureLegacyImpayeeEnRetard(clientId, now.minusDays(jours)));
        BigDecimal plafond = client.getMontantPlafond() == null ? null : BigDecimal.valueOf(client.getMontantPlafond());
        String motif = null;
        if (retard) motif = "Une ou plusieurs factures sont échues et impayées.";
        else if (plafond != null && encours.compareTo(plafond) > 0) motif = "Le plafond d'encours autorisé est dépassé.";
        else if (client.getMontantPlafondEcheance() != null && periode.compareTo(client.getMontantPlafondEcheance()) >= 0)
            motif = "Le plafond de facturation de la période d'échéance est atteint.";
        return new CompteClientResponse(clientId, client.getTypeClient() == null ? TypeClient.PARTICULIER.name() : client.getTypeClient().name(),
                client.getRaisonSociale(), client.getNumeroEntreprise(), compte != null, compte != null && compte.isActif(),
                client.getMontantRemise() == null ? BigDecimal.ZERO : BigDecimal.valueOf(client.getMontantRemise()),
                compte == null ? BigDecimal.ZERO : compte.getSoldeCredit(), plafond, jours,
                client.getMontantPlafondEcheance(), encours, periode, retard, motif == null, motif,
                client.getRccm(), client.getRib(), compte == null ? null : compte.getDateCreation());
    }

    @Transactional
    public CompteClientResponse ajouterCredit(Long clientId, AjoutCreditRequest request) {
        if (request == null || request.montant() == null || request.montant().signum() <= 0
                || request.montant().scale() > 2 || request.montant().precision() > 15)
            throw new IllegalArgumentException("Le crédit doit être un montant positif valide (maximum 2 décimales).");
        CompteClient compte = compteRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Aucun compte financier n'est ouvert pour ce client."));
        if (!compte.isActif()) throw new IllegalStateException("Le compte client est désactivé.");
        BigDecimal montant = request.montant().setScale(2, java.math.RoundingMode.HALF_UP);
        compte.setSoldeCredit(compte.getSoldeCredit().add(montant));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long actorId = auth == null ? null : userRepository.findByUsername(auth.getName()).or(() -> userRepository.findByEmail(auth.getName())).map(u -> u.getId()).orElse(null);
        mouvementRepository.save(MouvementCreditClient.builder().compteClient(compte).montant(montant)
                .commentaire(request.commentaire() == null ? null : request.commentaire().trim()).acteurId(actorId).build());
        compteRepository.save(compte);
        return lire(clientId);
    }

    @Transactional(readOnly = true)
    public List<sn.oas.facturation.features.client.dto.MouvementCreditResponse> mouvements(Long clientId) {
        CompteClient c = compteRepository.findByClientId(clientId).orElseThrow(() -> new ResourceNotFoundException("Compte client introuvable"));
        return mouvementRepository.findTop100ByCompteClientIdOrderByDateCreationDesc(c.getId()).stream()
                .map(sn.oas.facturation.features.client.dto.MouvementCreditResponse::from).toList();
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
