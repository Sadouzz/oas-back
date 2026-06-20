package sn.oas.facturation.bonDeSortie.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.auth.data.entity.User;
import sn.oas.facturation.auth.repository.UserRepository;
import sn.oas.facturation.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.bonDeSortie.data.entity.LigneBonDeSortiePiece;

import sn.oas.facturation.piecedetache.data.entity.PDP;
import sn.oas.facturation.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.piecedetache.data.entity.StockMouvement;
import sn.oas.facturation.bonDeSortie.data.enums.StatutBon;
import sn.oas.facturation.piecedetache.data.enums.TypeMouvement;
import sn.oas.facturation.bonDeSortie.dto.BonDeSortieRequest;
import sn.oas.facturation.bonDeSortie.dto.LignePieceRequest;

import sn.oas.facturation.bonDeSortie.repository.BonDeSortieRepository;
import sn.oas.facturation.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.piecedetache.repository.StockMouvementRepository;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import sn.oas.facturation.vehicule.repository.VehiculeRepository;import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.ficheAtelier.data.enums.StatutReparation;
import sn.oas.facturation.ficheAtelier.repository.FicheAtelierRepository;

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
    private final FicheAtelierRepository ficheAtelierRepository;
    private final sn.oas.facturation.facture.service.FactureService factureService;


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

        if (request.lignesPieces() != null) {
            for (LignePieceRequest ligneReq : request.lignesPieces()) {
                PDP pdp = getPDP(ligneReq.pieceId());
                if (ligneReq.quantite() == null || ligneReq.quantite() <= 0) {
                    throw new IllegalArgumentException(
                            "La quantité doit être supérieure à zéro pour la pièce id=" + ligneReq.pieceId());
                }
                Integer prixPiece = (pdp.getPrix() != null) ? pdp.getPrix().intValue() : 0;
                LigneBonDeSortiePiece ligne = LigneBonDeSortiePiece.builder()
                        .bonDeSortie(bon)
                        .piece(pdp)
                        .quantite(ligneReq.quantite())
                        .prix(prixPiece)
                        .build();
                bon.getLignesBonDeSortiePieces().add(ligne);
            }
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

        for (LigneBonDeSortiePiece ligne : bon.getLignesBonDeSortiePieces()) {
            PDP pdp = ligne.getPiece();
            int quantite = ligne.getQuantite();

            int stockMagasinDisponible = pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0;
            if (stockMagasinDisponible < quantite) {
                throw new IllegalArgumentException(
                        "Stock magasin insuffisant pour la pièce " + pdp.getReference()
                        + ". Disponible : " + stockMagasinDisponible + ", demandé : " + quantite);
            }

            int magasinAvant = stockMagasinDisponible;
            int atelierAvant = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0;

            pdp.setStockMagasin(magasinAvant - quantite);
            pdp.setStockAtelier(atelierAvant + quantite);
            pdp.setQteReelle(pdp.getStockMagasin() + pdp.getStockAtelier());
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
        
        if (bon.getFicheAtelier() != null && bon.getFicheAtelier().getStatut() == StatutReparation.EN_ATTENTE_SORTIE) {
            FicheAtelier fiche = bon.getFicheAtelier();
            fiche.setStatut(StatutReparation.EN_COURS);
            ficheAtelierRepository.save(fiche);
            
            // Génération automatique de la facture
            factureService.createFactureAuto(fiche);
        }

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
        boolean hasPieces = request.lignesPieces() != null && !request.lignesPieces().isEmpty();
        if (!hasPieces) {
            throw new IllegalArgumentException("Le bon de sortie doit contenir au moins une pièce");
        }
    }

    private PDP getPDP(Long pieceId) {
        PieceDetache piece = pieceDetacheRepository.findById(pieceId)
                .orElseThrow(() -> new RuntimeException("Pièce introuvable avec l'id : " + pieceId));
        piece = (PieceDetache) org.hibernate.Hibernate.unproxy(piece);
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