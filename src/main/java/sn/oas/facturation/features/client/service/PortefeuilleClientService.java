package sn.oas.facturation.features.client.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.data.entity.MouvementPortefeuilleClient;
import sn.oas.facturation.features.client.data.entity.PortefeuilleClient;
import sn.oas.facturation.features.client.data.enums.TypeMouvementPortefeuille;
import sn.oas.facturation.features.client.dto.AlimentationPortefeuilleRequest;
import sn.oas.facturation.features.client.dto.MouvementPortefeuilleResponse;
import sn.oas.facturation.features.client.dto.PortefeuilleClientResponse;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.client.repository.MouvementPortefeuilleClientRepository;
import sn.oas.facturation.features.client.repository.PortefeuilleClientRepository;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.recu.data.entity.Recu;
import sn.oas.facturation.features.user.repository.UserRepository;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortefeuilleClientService {
    public static final String MODE_PAIEMENT = "SOLDE_PREPAYE";

    private final ClientRepository clientRepository;
    private final PortefeuilleClientRepository portefeuilleRepository;
    private final MouvementPortefeuilleClientRepository mouvementRepository;
    private final UserRepository userRepository;
    private final DocumentNumberGeneratorService documentNumberGeneratorService;

    @Transactional(readOnly = true)
    public PortefeuilleClientResponse lire(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));
        Garage garage = garageCourantObligatoire();
        PortefeuilleClient portefeuille = portefeuilleRepository.findByClientIdAndGarageId(clientId, garage.getId()).orElse(null);
        if (portefeuille == null) {
            return new PortefeuilleClientResponse(client.getId(), BigDecimal.ZERO, null, null, List.of());
        }
        List<MouvementPortefeuilleResponse> mouvements = mouvementRepository
                .findTop100ByPortefeuilleIdOrderByDateCreationDesc(portefeuille.getId()).stream()
                .map(MouvementPortefeuilleResponse::from).toList();
        return new PortefeuilleClientResponse(client.getId(), portefeuille.getSolde(), portefeuille.getDateCreation(),
                portefeuille.getDateModification(), mouvements);
    }

    @Transactional
    public PortefeuilleClientResponse alimenter(Long clientId, AlimentationPortefeuilleRequest request) {
        if (request == null || request.montant() == null || request.montant().signum() <= 0
                || request.montant().scale() > 2 || request.montant().precision() > 15) {
            throw new IllegalArgumentException("Le versement doit être un montant positif valide (maximum 2 décimales).");
        }
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));
        Garage garage = garageCourantObligatoire();
        PortefeuilleClient portefeuille = getOrCreateForUpdate(client, garage);
        BigDecimal montant = request.montant().setScale(2, RoundingMode.HALF_UP);
        portefeuille.setSolde(portefeuille.getSolde().add(montant));
        portefeuilleRepository.save(portefeuille);
        mouvementRepository.save(MouvementPortefeuilleClient.builder()
                .portefeuille(portefeuille)
                .type(TypeMouvementPortefeuille.ALIMENTATION)
                .montant(montant)
                .modePaiement(normaliserMode(request.modePaiement()))
                .commentaire(request.commentaire() == null ? null : request.commentaire().trim())
                .acteurId(getActeurConnecteId())
                .build());
        return lire(clientId);
    }

    @Transactional
    public void debiterPourFacture(Facture facture, Recu recu, BigDecimal montant) {
        if (facture == null || facture.getClient() == null || facture.getGarage() == null) {
            throw new IllegalArgumentException("La facture doit être rattachée à un client pour utiliser le solde prépayé.");
        }
        PortefeuilleClient portefeuille = portefeuilleRepository.findByClientIdForUpdate(facture.getClient().getId(), facture.getGarage().getId())
                .orElseThrow(() -> new IllegalArgumentException("Ce client ne possède pas de solde prépayé disponible."));
        BigDecimal montantNormalise = montant.setScale(2, RoundingMode.HALF_UP);
        if (portefeuille.getSolde().compareTo(montantNormalise) < 0) {
            throw new IllegalArgumentException("Le solde prépayé est insuffisant. Solde disponible : "
                    + portefeuille.getSolde().toPlainString() + " F CFA.");
        }
        portefeuille.setSolde(portefeuille.getSolde().subtract(montantNormalise));
        portefeuilleRepository.save(portefeuille);
        mouvementRepository.save(MouvementPortefeuilleClient.builder()
                .portefeuille(portefeuille)
                .type(TypeMouvementPortefeuille.PAIEMENT_FACTURE)
                .montant(montantNormalise)
                .modePaiement(MODE_PAIEMENT)
                .commentaire("Paiement de la facture " + facture.getNumero())
                .acteurId(getActeurConnecteId())
                .facture(facture)
                .recu(recu)
                .build());
    }

    private PortefeuilleClient getOrCreateForUpdate(Client client, Garage garage) {
        return portefeuilleRepository.findByClientIdForUpdate(client.getId(), garage.getId()).orElseGet(() -> {
            try {
                return portefeuilleRepository.saveAndFlush(PortefeuilleClient.builder()
                        .client(client).garage(garage).solde(BigDecimal.ZERO).build());
            } catch (org.springframework.dao.DataIntegrityViolationException concurrentCreation) {
                return portefeuilleRepository.findByClientIdForUpdate(client.getId(), garage.getId())
                        .orElseThrow(() -> concurrentCreation);
            }
        });
    }

    private Garage garageCourantObligatoire() {
        Garage garage = documentNumberGeneratorService.getCurrentGarage();
        if (garage == null) throw new IllegalStateException("Aucun garage actif n'est associé à cette opération.");
        return garage;
    }

    private Long getActeurConnecteId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByUsername(auth.getName()).or(() -> userRepository.findByEmail(auth.getName()))
                .map(user -> user.getId()).orElse(null);
    }

    private String normaliserMode(String mode) {
        if (mode == null || mode.isBlank()) return null;
        String value = mode.trim().toUpperCase(java.util.Locale.ROOT);
        return switch (value) {
            case "ESPECE", "CHEQUE", "VIREMENT", "CARTE", "AUTRE" -> value;
            default -> throw new IllegalArgumentException("Mode de versement invalide.");
        };
    }
}
