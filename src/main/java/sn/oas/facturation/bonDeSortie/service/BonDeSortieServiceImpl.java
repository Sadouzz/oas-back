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
import sn.oas.facturation.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.notification.service.AgentNotificationService;
import sn.oas.facturation.auth.data.enums.Role;
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
    private final OrdreReparationRepository ordreReparationRepository;
    private final sn.oas.facturation.facture.service.FactureService factureService;
    private final AgentNotificationService agentNotificationService;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;
    private final sn.oas.facturation.bonDeSortie.repository.BonDeSortieHistoriqueRepository bonDeSortieHistoriqueRepository;

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
                .garage(agentEmetteur != null ? agentEmetteur.getGarage() : null)
                .remarque(request.remarque())
                .build();

        if (request.lignesPieces() != null) {
            for (LignePieceRequest ligneReq : request.lignesPieces()) {
                PDP pdp = getPDP(ligneReq.pieceId());
                if (ligneReq.quantite() == null || ligneReq.quantite() <= 0) {
                    throw new IllegalArgumentException(
                            "La quantité doit être supérieure à zéro pour la pièce id=" + ligneReq.pieceId());
                }
                Double prixPiece = pdp.getPrixUnitaire() != null ? pdp.getPrixUnitaire() : 0.0;
                LigneBonDeSortiePiece ligne = LigneBonDeSortiePiece.builder()
                        .bonDeSortie(bon)
                        .piece(pdp)
                        .quantite(ligneReq.quantite())
                        .prix(prixPiece.intValue())
                        .build();
                bon.getLignesBonDeSortiePieces().add(ligne);
            }
        }

        if (request.ordreReparationId() != null) {
            OrdreReparation fiche = ordreReparationRepository.findById(request.ordreReparationId())
                    .orElseThrow(() -> new IllegalArgumentException("Fiche atelier introuvable"));
            bon.setOrdreReparation(fiche);
        }

        // Mouvement de stock à la création du BS : stockMagasin diminue, stockAtelier
        // augmente, qteReelle inchangée
        if (bon.getLignesBonDeSortiePieces() != null) {
            for (LigneBonDeSortiePiece ligne : bon.getLignesBonDeSortiePieces()) {
                PDP pdp = ligne.getPiece();
                double quantite = (double) ligne.getQuantite();

                Double stockMagasinDisponible = pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0.0;
                if (stockMagasinDisponible < quantite) {
                    throw new IllegalArgumentException(
                            "Stock magasin insuffisant pour la pièce " + pdp.getDesignation()
                                    + ". Disponible : " + stockMagasinDisponible + ", demandé : " + quantite);
                }

                Double magasinAvant = stockMagasinDisponible;
                Double atelierAvant = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0.0;

                pdp.setStockMagasin(magasinAvant - quantite);
                pdp.setStockAtelier(atelierAvant + quantite);
                pieceDetacheRepository.save(pdp);

                stockMouvementRepository.save(StockMouvement.builder()
                        .type(TypeMouvement.SORTIE_MAGASIN)
                        .quantite(quantite)
                        .stockMagasinAvant(magasinAvant)
                        .stockAtelierAvant(atelierAvant)
                        .stockMagasinApres(pdp.getStockMagasin())
                        .stockAtelierApres(pdp.getStockAtelier())
                        .stockReelApres(pdp.getQteReelle())
                        .prenom(agentEmetteur != null ? agentEmetteur.getFirstName() : "")
                        .nom(agentEmetteur != null ? agentEmetteur.getLastName() : "")
                        .numDocument(bon.getReference())
                        .typeDocument("Bon de sortie")
                        .numeroSerie(pdp.getReference())
                        .immatriculation(bon.getVehicule() != null ? bon.getVehicule().getImmatriculation() : "")
                        .motif("Création Bon de sortie " + bon.getReference())
                        .piece(pdp)
                        .agent(agentEmetteur)
                        .garage(bon.getGarage())
                        .build());
            }
        }

        BonDeSortie saved = bonDeSortieRepository.save(bon);

        // Enregistre l'historique du BS pour chaque pièce
        if (saved.getLignesBonDeSortiePieces() != null && !saved.getLignesBonDeSortiePieces().isEmpty()) {
            for (LigneBonDeSortiePiece ligne : saved.getLignesBonDeSortiePieces()) {
                PDP pdp = ligne.getPiece();
                bonDeSortieHistoriqueRepository.save(sn.oas.facturation.bonDeSortie.data.entity.BonDeSortieHistorique
                        .builder()
                        .bonDeSortie(saved)
                        .piece(pdp)
                        .quantite((double) ligne.getQuantite())
                        .stockMagasin(pdp.getStockMagasin())
                        .stockAtelier(pdp.getStockAtelier())
                        .qteReelle(pdp.getQteReelle())
                        .prenom(agentEmetteur != null ? agentEmetteur.getFirstName() : "")
                        .nom(agentEmetteur != null ? agentEmetteur.getLastName() : "")
                        .numBs(saved.getReference())
                        .numeroSerie(pdp != null ? pdp.getReference() : "")
                        .immatriculation(saved.getVehicule() != null ? saved.getVehicule().getImmatriculation() : "")
                        .designation(pdp != null ? pdp.getDesignation() : "")
                        .statut("SORTIE")
                        .motif("Sortie magasin vers atelier pour BS " + saved.getReference())
                        .agent(agentEmetteur)
                        .garage(saved.getGarage())
                        .build());
            }
        } else {
            bonDeSortieHistoriqueRepository.save(sn.oas.facturation.bonDeSortie.data.entity.BonDeSortieHistorique
                    .builder()
                    .bonDeSortie(saved)
                    .prenom(agentEmetteur != null ? agentEmetteur.getFirstName() : "")
                    .nom(agentEmetteur != null ? agentEmetteur.getLastName() : "")
                    .numBs(saved.getReference())
                    .immatriculation(saved.getVehicule() != null ? saved.getVehicule().getImmatriculation() : "")
                    .statut("SORTIE")
                    .motif("Création du bon de sortie " + saved.getReference())
                    .agent(agentEmetteur)
                    .garage(saved.getGarage())
                    .build());
        }

        if (saved.getOrdreReparation() != null) {
            OrdreReparation fiche = saved.getOrdreReparation();
            fiche.setBonDeSortie(saved);
            ordreReparationRepository.save(fiche);
            agentNotificationService.notifyRole(Role.AGENT_MAGASIN,
                    "Nouveau Bon de Sortie",
                    "Le bon de sortie " + saved.getReference() + " est en attente de validation pour la fiche "
                            + fiche.getNumero());
        }
        return saved;
    }

    @Transactional
    @Override
    public BonDeSortie valider(Long id) {
        BonDeSortie bon = getById(id);
        Agent agentValidateur = getAgentConnecte();

        if (bon.getStatut() == StatutBon.VALIDE) {
            throw new IllegalStateException("Le bon de sortie " + bon.getReference() + " est déjà validé");
        }

        // A la validation du BS : stockAtelier diminue (la pièce va sur le véhicule),
        // qteReelle reste le même
        for (LigneBonDeSortiePiece ligne : bon.getLignesBonDeSortiePieces()) {
            PDP pdp = ligne.getPiece();
            double quantite = (double) ligne.getQuantite();

            Double magasinAvant = pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0.0;
            Double atelierAvant = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0.0;

            pdp.setStockAtelier(Math.max(0.0, atelierAvant - quantite));
            pieceDetacheRepository.save(pdp);

            stockMouvementRepository.save(StockMouvement.builder()
                    .type(TypeMouvement.SORTIE_ATELIER)
                    .quantite(quantite)
                    .stockMagasinAvant(magasinAvant)
                    .stockAtelierAvant(atelierAvant)
                    .stockMagasinApres(pdp.getStockMagasin())
                    .stockAtelierApres(pdp.getStockAtelier())
                    .stockReelApres(pdp.getQteReelle())
                    .prenom(agentValidateur != null ? agentValidateur.getFirstName() : "")
                    .nom(agentValidateur != null ? agentValidateur.getLastName() : "")
                    .numDocument(bon.getReference())
                    .typeDocument("Bon de sortie")
                    .numeroSerie(pdp.getReference())
                    .immatriculation(bon.getVehicule() != null ? bon.getVehicule().getImmatriculation() : "")
                    .motif("Validation Bon de sortie " + bon.getReference())
                    .piece(pdp)
                    .agent(agentValidateur)
                    .garage(bon.getGarage())
                    .build());
        }

        bon.setStatut(StatutBon.VALIDE);
        bon.setAgentValidateur(agentValidateur);
        bon.setDateValidation(LocalDateTime.now());

        // Enregistre l'historique du BS pour chaque pièce
        if (bon.getLignesBonDeSortiePieces() != null && !bon.getLignesBonDeSortiePieces().isEmpty()) {
            for (LigneBonDeSortiePiece ligne : bon.getLignesBonDeSortiePieces()) {
                PDP pdp = ligne.getPiece();
                bonDeSortieHistoriqueRepository.save(sn.oas.facturation.bonDeSortie.data.entity.BonDeSortieHistorique
                        .builder()
                        .bonDeSortie(bon)
                        .piece(pdp)
                        .quantite((double) ligne.getQuantite())
                        .stockMagasin(pdp.getStockMagasin())
                        .stockAtelier(pdp.getStockAtelier())
                        .qteReelle(pdp.getQteReelle())
                        .prenom(agentValidateur != null ? agentValidateur.getFirstName() : "")
                        .nom(agentValidateur != null ? agentValidateur.getLastName() : "")
                        .numBs(bon.getReference())
                        .numeroSerie(pdp != null ? pdp.getReference() : "")
                        .immatriculation(bon.getVehicule() != null ? bon.getVehicule().getImmatriculation() : "")
                        .designation(pdp != null ? pdp.getDesignation() : "")
                        .statut("SORTIE ATELIER")
                        .motif("Validation du bon de sortie " + bon.getReference())
                        .agent(agentValidateur)
                        .garage(bon.getGarage())
                        .build());
            }
        } else {
            bonDeSortieHistoriqueRepository
                    .save(sn.oas.facturation.bonDeSortie.data.entity.BonDeSortieHistorique.builder()
                            .bonDeSortie(bon)
                            .prenom(agentValidateur != null ? agentValidateur.getFirstName() : "")
                            .nom(agentValidateur != null ? agentValidateur.getLastName() : "")
                            .numBs(bon.getReference())
                            .immatriculation(bon.getVehicule() != null ? bon.getVehicule().getImmatriculation() : "")
                            .statut("SORTIE ATELIER")
                            .motif("Validation du bon de sortie " + bon.getReference())
                            .agent(agentValidateur)
                            .garage(bon.getGarage())
                            .build());
        }

        if (bon.getOrdreReparation() != null) {
            OrdreReparation fiche = bon.getOrdreReparation();
            // Advancing the status if it's in one of the states waiting for parts
            if (fiche.getStatut() == StatutOrdreReparation.EN_ATTENTE_SORTIE ||
                    fiche.getStatut() == StatutOrdreReparation.PROFORMA_VALIDE ||
                    fiche.getStatut() == StatutOrdreReparation.EN_ATTENTE_COMMANDE) {

                fiche.setStatut(StatutOrdreReparation.EN_ATTENTE_MECANICIEN);
                agentNotificationService.notifyRole(Role.CHEF_ATELIER,
                        "Mécanicien à assigner",
                        "Le bon de sortie " + bon.getReference() + " a été validé. La fiche " + fiche.getNumero()
                                + " est prête. Veuillez assigner les mécaniciens finaux.");
                ordreReparationRepository.save(fiche);

                // Génération automatique de la facture
                factureService.createFactureAuto(fiche);
            }
        }

        return bonDeSortieRepository.save(bon);
    }

    @Transactional
    @Override
    public BonDeSortie retournerPiece(Long id, Long pieceId) {
        BonDeSortie bon = getById(id);
        Agent agentConnecte = getAgentConnecte();

        if (bon.getStatut() == StatutBon.VALIDE) {
            throw new IllegalStateException("Impossible d'effectuer un retour sur un bon de sortie déjà validé");
        }

        LigneBonDeSortiePiece ligneARetirer = null;
        for (LigneBonDeSortiePiece l : bon.getLignesBonDeSortiePieces()) {
            if (l.getPiece().getId().equals(pieceId)) {
                ligneARetirer = l;
                break;
            }
        }

        if (ligneARetirer == null) {
            throw new IllegalArgumentException("La pièce id=" + pieceId + " n'est pas présente dans ce bon de sortie");
        }

        PDP pdp = ligneARetirer.getPiece();
        double quantite = (double) ligneARetirer.getQuantite();

        Double magasinAvant = pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0.0;
        Double atelierAvant = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0.0;

        // Re-crédite le stock magasin et déduit le stock atelier
        pdp.setStockMagasin(magasinAvant + quantite);
        pdp.setStockAtelier(Math.max(0.0, atelierAvant - quantite));
        pieceDetacheRepository.save(pdp);

        // Retire la ligne du BS
        bon.getLignesBonDeSortiePieces().remove(ligneARetirer);

        // Enregistre le mouvement de stock
        stockMouvementRepository.save(StockMouvement.builder()
                .type(TypeMouvement.RETOUR_MAGASIN)
                .quantite(quantite)
                .stockMagasinAvant(magasinAvant)
                .stockAtelierAvant(atelierAvant)
                .stockMagasinApres(pdp.getStockMagasin())
                .stockAtelierApres(pdp.getStockAtelier())
                .stockReelApres(pdp.getQteReelle())
                .prenom(agentConnecte != null ? agentConnecte.getFirstName() : "")
                .nom(agentConnecte != null ? agentConnecte.getLastName() : "")
                .numDocument(bon.getReference())
                .typeDocument("Bon de sortie")
                .numeroSerie(pdp.getReference())
                .immatriculation(bon.getVehicule() != null ? bon.getVehicule().getImmatriculation() : "")
                .motif("Retour pièce " + pdp.getReference() + " (BS " + bon.getReference() + ")")
                .piece(pdp)
                .agent(agentConnecte)
                .garage(bon.getGarage())
                .build());

        // Enregistre l'historique du BS
        bonDeSortieHistoriqueRepository.save(sn.oas.facturation.bonDeSortie.data.entity.BonDeSortieHistorique.builder()
                .bonDeSortie(bon)
                .piece(pdp)
                .quantite(-quantite)
                .stockMagasin(pdp.getStockMagasin())
                .stockAtelier(pdp.getStockAtelier())
                .qteReelle(pdp.getQteReelle())
                .prenom(agentConnecte != null ? agentConnecte.getFirstName() : "")
                .nom(agentConnecte != null ? agentConnecte.getLastName() : "")
                .numBs(bon.getReference())
                .numeroSerie(pdp.getReference())
                .immatriculation(bon.getVehicule() != null ? bon.getVehicule().getImmatriculation() : "")
                .designation(pdp.getDesignation())
                .statut("RETOUR")
                .motif("Retour de la pièce " + pdp.getReference() + " (" + pdp.getDesignation() + ") - Qté: "
                        + (int) quantite)
                .agent(agentConnecte)
                .garage(bon.getGarage())
                .build());

        return bonDeSortieRepository.save(bon);
    }

    @Override
    public List<sn.oas.facturation.bonDeSortie.data.entity.BonDeSortieHistorique> getHistorique(Long id) {
        return bonDeSortieHistoriqueRepository.findByBonDeSortieIdOrderByDateActionDesc(id);
    }

    @Override
    public List<sn.oas.facturation.bonDeSortie.data.entity.BonDeSortieHistorique> getAllHistorique() {
        return bonDeSortieHistoriqueRepository.findAllByOrderByDateActionDesc();
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
        return documentNumberGeneratorService
                .generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.BS);
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