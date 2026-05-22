package sn.oas.facturation.piecedetache.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.auth.data.entity.User;
import sn.oas.facturation.auth.repository.UserRepository;
import sn.oas.facturation.piecedetache.data.entity.BonDeSortie;
import sn.oas.facturation.piecedetache.data.entity.LigneBonDeSortie;
import sn.oas.facturation.piecedetache.data.entity.PDP;
import sn.oas.facturation.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.piecedetache.data.entity.StockMouvement;
import sn.oas.facturation.piecedetache.data.enums.StatutBon;
import sn.oas.facturation.piecedetache.data.enums.TypeMouvement;
import sn.oas.facturation.piecedetache.dto.BonDeSortieRequest;
import sn.oas.facturation.piecedetache.dto.LigneBonDeSortieRequest;
import sn.oas.facturation.piecedetache.repository.BonDeSortieRepository;
import sn.oas.facturation.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.piecedetache.repository.StockMouvementRepository;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import sn.oas.facturation.vehicule.repository.VehiculeRepository;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BonDeSortieServiceImpl implements BonDeSortieService {

    private final BonDeSortieRepository bonDeSortieRepository;
    private final PieceDetacheRepository pieceDetacheRepository;
    private final StockMouvementRepository stockMouvementRepository;
    private final VehiculeRepository vehiculeRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public BonDeSortie creer(BonDeSortieRequest request) {
        validerRequest(request);
        Agent agentEmetteur = getAgentConnecte();

        Client client = getClient(request.clientId());
        Vehicule vehicule = getVehicule(request.vehiculeId());

        if (!vehicule.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("Le véhicule ne correspond pas au client");
        }

        BonDeSortie bon = BonDeSortie.builder()
                .reference(genererReference())
                .client(client)
                .vehicule(vehicule)
                .agentEmetteur(agentEmetteur)
                .remarque(request.remarque())
                .build();

        for (LigneBonDeSortieRequest ligneReq : request.lignes()) {
            PDP pdp = getPDP(ligneReq.pieceId());
            if (ligneReq.quantite() == null || ligneReq.quantite() <= 0) {
                throw new IllegalArgumentException(
                        "La quantité doit être supérieure à zéro pour la pièce id=" + ligneReq.pieceId());
            }
            LigneBonDeSortie ligne = LigneBonDeSortie.builder()
                    .bonDeSortie(bon)
                    .piece(pdp)
                    .quantite(ligneReq.quantite())
                    .build();
            bon.getLignes().add(ligne);
        }

        return bonDeSortieRepository.save(bon);
    }

    @Transactional
    @Override
    public BonDeSortie valider(Long id) {
        BonDeSortie bon = getById(id);
        Agent agentValidateur = getAgentConnecte();

        if (bon.getStatut() == StatutBon.VALIDE) {
            throw new IllegalStateException("Le bon de sortie " + bon.getReference() + " est déjà validé");
        }

        for (LigneBonDeSortie ligne : bon.getLignes()) {
            PDP pdp = ligne.getPiece();
            int quantite = ligne.getQuantite();

            if (pdp.getStockMagasin() < quantite) {
                throw new IllegalArgumentException(
                        "Stock magasin insuffisant pour la pièce " + pdp.getReference()
                        + ". Disponible : " + pdp.getStockMagasin() + ", demandé : " + quantite);
            }

            int magasinAvant = pdp.getStockMagasin();
            int atelierAvant = pdp.getStockAtelier();

            pdp.setStockMagasin(magasinAvant - quantite);
            pdp.setStockAtelier(atelierAvant + quantite);
            pieceDetacheRepository.save(pdp);

            stockMouvementRepository.save(StockMouvement.builder()
                    .type(TypeMouvement.SORTIE)
                    .quantite(quantite)
                    .stockMagasinAvant(magasinAvant)
                    .stockAtelierAvant(atelierAvant)
                    .stockMagasinApres(pdp.getStockMagasin())
                    .stockAtelierApres(pdp.getStockAtelier())
                    .motif("Bon de sortie " + bon.getReference())
                    .piece(pdp)
                    .agent(agentValidateur)
                    .build());
        }

        bon.setStatut(StatutBon.VALIDE);
        bon.setAgentValidateur(agentValidateur);
        bon.setDateValidation(LocalDateTime.now());
        return bonDeSortieRepository.save(bon);
    }

    @Override
    public BonDeSortie getById(Long id) {
        return bonDeSortieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de sortie introuvable avec l'id : " + id));
    }

    @Override
    public List<BonDeSortie> getAll() {
        return bonDeSortieRepository.findAll();
    }

    @Override
    public List<BonDeSortie> getByStatut(StatutBon statut) {
        return bonDeSortieRepository.findByStatutOrderByDateDesc(statut);
    }

    @Override
    public List<BonDeSortie> getByClient(Long clientId) {
        return bonDeSortieRepository.findByClientIdOrderByDateDesc(clientId);
    }

    @Override
    public List<BonDeSortie> getByVehicule(Long vehiculeId) {
        return bonDeSortieRepository.findByVehiculeIdOrderByDateDesc(vehiculeId);
    }

    private String genererReference() {
        int annee = Year.now().getValue();
        long count = bonDeSortieRepository.countByAnnee(annee) + 1;
        return String.format("BS-%d-%04d", annee, count);
    }

    private void validerRequest(BonDeSortieRequest request) {
        if (request.clientId() == null) {
            throw new IllegalArgumentException("Le client est obligatoire");
        }
        if (request.vehiculeId() == null) {
            throw new IllegalArgumentException("Le véhicule est obligatoire");
        }
        if (request.lignes() == null || request.lignes().isEmpty()) {
            throw new IllegalArgumentException("Le bon de sortie doit contenir au moins une pièce");
        }
    }

    private PDP getPDP(Long pieceId) {
        PieceDetache piece = pieceDetacheRepository.findById(pieceId)
                .orElseThrow(() -> new RuntimeException("Pièce introuvable avec l'id : " + pieceId));
        if (!(piece instanceof PDP pdp)) {
            throw new IllegalArgumentException("La pièce id=" + pieceId + " n'est pas une PDP");
        }
        return pdp;
    }

    private Client getClient(Long clientId) {
        User user = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client introuvable avec l'id : " + clientId));
        if (!(user instanceof Client client)) {
            throw new IllegalArgumentException("L'utilisateur id=" + clientId + " n'est pas un client");
        }
        return client;
    }

    private Vehicule getVehicule(Long vehiculeId) {
        return vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new RuntimeException("Véhicule introuvable avec l'id : " + vehiculeId));
    }

    private Agent getAgentConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable"));
        if (!(user instanceof Agent agent)) {
            throw new IllegalStateException("Cette opération requiert un compte Agent");
        }
        return agent;
    }
}