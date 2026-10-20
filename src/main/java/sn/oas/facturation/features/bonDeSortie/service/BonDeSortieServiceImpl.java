package sn.oas.facturation.features.bonDeSortie.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.oas.facturation.features.user.repository.UserRepository;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.bonDeSortie.data.entity.LigneBonDeSortiePiece;

import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortieHistorique;
import sn.oas.facturation.features.bonDeSortie.repository.BonDeSortieHistoriqueRepository;
import sn.oas.facturation.features.facture.service.FactureService;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.data.entity.PieceMouvement;
import sn.oas.facturation.features.bonDeSortie.data.enums.StatutBon;
import sn.oas.facturation.features.piecedetache.data.enums.TypeMouvement;
import sn.oas.facturation.features.bonDeSortie.dto.BonDeSortieRequest;
import sn.oas.facturation.features.bonDeSortie.dto.LignePieceRequest;

import sn.oas.facturation.features.bonDeSortie.repository.BonDeSortieRepository;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.features.piecedetache.repository.PieceMouvementRepository;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.user.data.enums.Role;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.notification.service.AgentNotificationService;
import sn.oas.facturation.features.proforma.data.entity.Proforma;
import sn.oas.facturation.features.proforma.repository.ProformaRepository;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationPiece;
import sn.oas.facturation.features.garage.data.entity.Garage;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class BonDeSortieServiceImpl implements BonDeSortieService {

    private final BonDeSortieRepository bonDeSortieRepository;
    private final PieceDetacheRepository pieceDetacheRepository;
    private final PieceMouvementRepository pieceMouvementRepository;
    private final VehiculeRepository vehiculeRepository;
    private final UserRepository userRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final FactureService factureService;
    private final AgentNotificationService agentNotificationService;
    private final sn.oas.facturation.features.piecedetache.service.StockThresholdNotificationService stockThresholdNotificationService;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;
    private final BonDeSortieHistoriqueRepository bonDeSortieHistoriqueRepository;
    private final ProformaRepository proformaRepository;

    @Transactional
    @Override
    public BonDeSortie creerDepuisProforma(Long proformaId) {
        if (proformaId == null) {
            throw new IllegalArgumentException("L'ID du proforma est obligatoire.");
        }

        Proforma proforma = proformaRepository.findById(proformaId)
                .orElseThrow(() -> new IllegalArgumentException("Proforma introuvable avec l'id: " + proformaId));

        if (proforma.getBs() != null) {
            throw new IllegalStateException("Un bon de sortie (" + proforma.getBs().getReference() + ") est déjà lié à ce proforma.");
        }

        var existingBs = bonDeSortieRepository.findByProformaId(proformaId);
        if (existingBs.isPresent()) {
            throw new IllegalStateException("Un bon de sortie (" + existingBs.get().getReference() + ") est déjà lié à ce proforma.");
        }

        if (proforma.getStatut() == sn.oas.facturation.features.facturation.data.enums.StatutFacturation.REJETE 
                || proforma.getStatut() == sn.oas.facturation.features.facturation.data.enums.StatutFacturation.ANNULEE) {
            throw new IllegalStateException("Impossible de créer un bon de sortie pour un proforma rejeté ou annulé.");
        }

        Vehicule vehicule = proforma.getVehicule();
        if (vehicule == null && proforma.getOrdreReparation() != null) {
            vehicule = proforma.getOrdreReparation().getVehicule();
        }
        Client client = proforma.getClient();
        if (client == null && proforma.getOrdreReparation() != null) {
            client = proforma.getOrdreReparation().getClient();
        }
        if (client == null && vehicule != null) {
            client = vehicule.getClient();
        }

        if (client == null) {
            throw new IllegalArgumentException("Impossible de créer un bon de sortie : aucun client associé au proforma.");
        }
        if (vehicule == null) {
            throw new IllegalArgumentException("Impossible de créer un bon de sortie : aucun véhicule associé au proforma.");
        }
        sn.oas.facturation.features.vehicule.service.VehiculeActivationPolicy.requireActive(vehicule);

        Agent agentEmetteur = getAgentConnecte();
        if (agentEmetteur == null) {
            agentEmetteur = proforma.getAgent();
        }

        Garage garage = proforma.getGarage();
        if (garage == null && agentEmetteur != null) {
            garage = agentEmetteur.getGarage();
        }
        if (garage == null && proforma.getOrdreReparation() != null) {
            garage = proforma.getOrdreReparation().getGarage();
        }

        OrdreReparation ordre = proforma.getOrdreReparation();

        BonDeSortie bon = BonDeSortie.builder()
                .reference(genererReference())
                .client(client)
                .vehicule(vehicule)
                .agentEmetteur(agentEmetteur)
                .garage(garage)
                .proforma(proforma)
                .remarque("Créé depuis le proforma " + (proforma.getNumero() != null ? proforma.getNumero() : proforma.getId()))
                .build();

        if (ordre != null && ordre.getBonDeSortie() == null) {
            bon.setOrdreReparation(ordre);
        }

        if (proforma.getLignesFacturationPieces() != null) {
            for (LigneFacturationPiece ligneProforma : proforma.getLignesFacturationPieces()) {
                if (ligneProforma.getPiece() != null) {
                    PDP pdp = resolvePDP(ligneProforma.getPiece());
                    if (pdp == null && ligneProforma.getPiece().getId() != null) {
                        pdp = getPDP(ligneProforma.getPiece().getId());
                    }
                    if (pdp != null) {
                        int qte = ligneProforma.getQuantite() != null && ligneProforma.getQuantite() > 0 
                                ? ligneProforma.getQuantite() : 1;
                        int prix = ligneProforma.getPrix() != null 
                                ? ligneProforma.getPrix() 
                                : (pdp.getPrixUnitaire() != null ? pdp.getPrixUnitaire().intValue() : 0);

                        LigneBonDeSortiePiece ligneBs = LigneBonDeSortiePiece.builder()
                                .bonDeSortie(bon)
                                .piece(pdp)
                                .quantite(qte)
                                .prix(prix)
                                .build();
                        bon.getLignesBonDeSortiePieces().add(ligneBs);
                    }
                }
            }
        }

        // Mouvement de stock à la création du BS
        if (bon.getLignesBonDeSortiePieces() != null) {
            for (LigneBonDeSortiePiece ligne : bon.getLignesBonDeSortiePieces()) {
                PDP pdp = resolvePDP(ligne.getPiece());
                if (pdp != null) {
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
                    stockThresholdNotificationService.notifyIfThresholdCrossed(pdp, magasinAvant);

                    pieceMouvementRepository.save(PieceMouvement.builder()
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
                            .motif("Création Bon de sortie " + bon.getReference() + " depuis proforma " + (proforma.getNumero() != null ? proforma.getNumero() : proforma.getId()))
                            .piece(pdp)
                            .agent(agentEmetteur)
                            .garage(bon.getGarage())
                            .build());
                }
            }
        }

        BonDeSortie saved = bonDeSortieRepository.save(bon);

        // Enregistre l'historique du BS pour chaque pièce
        if (saved.getLignesBonDeSortiePieces() != null && !saved.getLignesBonDeSortiePieces().isEmpty()) {
            for (LigneBonDeSortiePiece ligne : saved.getLignesBonDeSortiePieces()) {
                PDP pdp = resolvePDP(ligne.getPiece());
                PieceDetache piece = pdp != null ? pdp : (PieceDetache) org.hibernate.Hibernate.unproxy(ligne.getPiece());
                bonDeSortieHistoriqueRepository.save(BonDeSortieHistorique
                        .builder()
                        .bonDeSortie(saved)
                        .piece(piece)
                        .quantite((double) ligne.getQuantite())
                        .stockMagasin(pdp != null ? pdp.getStockMagasin() : 0.0)
                        .stockAtelier(pdp != null ? pdp.getStockAtelier() : 0.0)
                        .qteReelle(pdp != null ? pdp.getQteReelle() : 0.0)
                        .prenom(agentEmetteur != null ? agentEmetteur.getFirstName() : "")
                        .nom(agentEmetteur != null ? agentEmetteur.getLastName() : "")
                        .numBs(saved.getReference())
                        .numeroSerie(piece != null ? piece.getReference() : "")
                        .immatriculation(saved.getVehicule() != null ? saved.getVehicule().getImmatriculation() : "")
                        .designation(piece != null ? piece.getDesignation() : "")
                        .statut("SORTIE")
                        .motif("Sortie magasin vers atelier pour BS " + saved.getReference() + " (Proforma " + (proforma.getNumero() != null ? proforma.getNumero() : proforma.getId()) + ")")
                        .agent(agentEmetteur)
                        .garage(saved.getGarage())
                        .build());
            }
        } else {
            bonDeSortieHistoriqueRepository.save(BonDeSortieHistorique
                    .builder()
                    .bonDeSortie(saved)
                    .prenom(agentEmetteur != null ? agentEmetteur.getFirstName() : "")
                    .nom(agentEmetteur != null ? agentEmetteur.getLastName() : "")
                    .numBs(saved.getReference())
                    .immatriculation(saved.getVehicule() != null ? saved.getVehicule().getImmatriculation() : "")
                    .statut("SORTIE")
                    .motif("Création du bon de sortie " + saved.getReference() + " depuis proforma " + (proforma.getNumero() != null ? proforma.getNumero() : proforma.getId()))
                    .agent(agentEmetteur)
                    .garage(saved.getGarage())
                    .build());
        }

        proforma.setBs(saved);
        proformaRepository.save(proforma);

        return saved;
    }

    @Transactional
    @Override
    public BonDeSortie creer(BonDeSortieRequest request) {
        validerRequest(request);
        Agent agentEmetteur = getAgentConnecte();

        Client client = getClient(request.clientId());
        Vehicule vehicule = getVehicule(request.vehiculeId());
        sn.oas.facturation.features.vehicule.service.VehiculeActivationPolicy.requireActive(vehicule);

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
                Double customPrix = ligneReq.getEffectivePrix();
                Double prixPiece = (customPrix != null && customPrix >= 0)
                        ? customPrix
                        : (pdp.getPrixUnitaire() != null ? pdp.getPrixUnitaire() : 0.0);
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
                PDP pdp = resolvePDP(ligne.getPiece());
                if (pdp != null) {
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
                    stockThresholdNotificationService.notifyIfThresholdCrossed(pdp, magasinAvant);

                    pieceMouvementRepository.save(PieceMouvement.builder()
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
        }

        BonDeSortie saved = bonDeSortieRepository.save(bon);

        // Enregistre l'historique du BS pour chaque pièce
        if (saved.getLignesBonDeSortiePieces() != null && !saved.getLignesBonDeSortiePieces().isEmpty()) {
            for (LigneBonDeSortiePiece ligne : saved.getLignesBonDeSortiePieces()) {
                PDP pdp = resolvePDP(ligne.getPiece());
                PieceDetache piece = pdp != null ? pdp : (PieceDetache) org.hibernate.Hibernate.unproxy(ligne.getPiece());
                bonDeSortieHistoriqueRepository.save(BonDeSortieHistorique
                        .builder()
                        .bonDeSortie(saved)
                        .piece(piece)
                        .quantite((double) ligne.getQuantite())
                        .stockMagasin(pdp != null ? pdp.getStockMagasin() : 0.0)
                        .stockAtelier(pdp != null ? pdp.getStockAtelier() : 0.0)
                        .qteReelle(pdp != null ? pdp.getQteReelle() : 0.0)
                        .prenom(agentEmetteur != null ? agentEmetteur.getFirstName() : "")
                        .nom(agentEmetteur != null ? agentEmetteur.getLastName() : "")
                        .numBs(saved.getReference())
                        .numeroSerie(piece != null ? piece.getReference() : "")
                        .immatriculation(saved.getVehicule() != null ? saved.getVehicule().getImmatriculation() : "")
                        .designation(piece != null ? piece.getDesignation() : "")
                        .statut("SORTIE")
                        .motif("Sortie magasin vers atelier pour BS " + saved.getReference())
                        .agent(agentEmetteur)
                        .garage(saved.getGarage())
                        .build());
            }
        } else {
            bonDeSortieHistoriqueRepository.save(BonDeSortieHistorique
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
            agentNotificationService.notifyRoleInGarage(Role.AGENT_MAGASIN, saved.getGarage(),
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
            PDP pdp = resolvePDP(ligne.getPiece());
            if (pdp != null) {
                double quantite = (double) ligne.getQuantite();

                Double magasinAvant = pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0.0;
                Double atelierAvant = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0.0;

                pdp.setStockAtelier(Math.max(0.0, atelierAvant - quantite));
                pieceDetacheRepository.save(pdp);

                pieceMouvementRepository.save(PieceMouvement.builder()
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
        }

        bon.setStatut(StatutBon.VALIDE);
        bon.setAgentValidateur(agentValidateur);
        bon.setDateValidation(LocalDateTime.now());

        // Enregistre l'historique du BS pour chaque pièce
        if (bon.getLignesBonDeSortiePieces() != null && !bon.getLignesBonDeSortiePieces().isEmpty()) {
            for (LigneBonDeSortiePiece ligne : bon.getLignesBonDeSortiePieces()) {
                PDP pdp = resolvePDP(ligne.getPiece());
                PieceDetache piece = pdp != null ? pdp : (PieceDetache) org.hibernate.Hibernate.unproxy(ligne.getPiece());
                bonDeSortieHistoriqueRepository.save(BonDeSortieHistorique
                        .builder()
                        .bonDeSortie(bon)
                        .piece(piece)
                        .quantite((double) ligne.getQuantite())
                        .stockMagasin(pdp != null ? pdp.getStockMagasin() : 0.0)
                        .stockAtelier(pdp != null ? pdp.getStockAtelier() : 0.0)
                        .qteReelle(pdp != null ? pdp.getQteReelle() : 0.0)
                        .prenom(agentValidateur != null ? agentValidateur.getFirstName() : "")
                        .nom(agentValidateur != null ? agentValidateur.getLastName() : "")
                        .numBs(bon.getReference())
                        .numeroSerie(piece != null ? piece.getReference() : "")
                        .immatriculation(bon.getVehicule() != null ? bon.getVehicule().getImmatriculation() : "")
                        .designation(piece != null ? piece.getDesignation() : "")
                        .statut("SORTIE ATELIER")
                        .motif("Validation du bon de sortie " + bon.getReference())
                        .agent(agentValidateur)
                        .garage(bon.getGarage())
                        .build());
            }
        } else {
            bonDeSortieHistoriqueRepository
                    .save(BonDeSortieHistorique.builder()
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
            if (fiche.getStatut() == StatutOrdreReparation.BON_DE_SORTIE ||
                    fiche.getStatut() == StatutOrdreReparation.BON_DE_COMMANDE ||
                    fiche.getStatut() == StatutOrdreReparation.PROFORMA) {

                fiche.setStatut(StatutOrdreReparation.ASSIGN_TECHNICIEN);
                agentNotificationService.notifyRoleInGarage(Role.CHEF_ATELIER, bon.getGarage(),
                        "Mécanicien à assigner",
                        "Le bon de sortie " + bon.getReference() + " a été validé. La fiche " + fiche.getNumero()
                                + " est prête. Veuillez assigner les mécaniciens finaux.");
                ordreReparationRepository.save(fiche);

                // Génération automatique de la facture
                //factureService.createFactureAuto(fiche);
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

        PDP pdp = resolvePDP(ligneARetirer.getPiece());
        PieceDetache piece = pdp != null ? pdp : (PieceDetache) org.hibernate.Hibernate.unproxy(ligneARetirer.getPiece());
        double quantite = (double) ligneARetirer.getQuantite();

        if (pdp != null) {
            Double magasinAvant = pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0.0;
            Double atelierAvant = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0.0;

            // Re-crédite le stock magasin et déduit le stock atelier
            pdp.setStockMagasin(magasinAvant + quantite);
            pdp.setStockAtelier(Math.max(0.0, atelierAvant - quantite));
            pieceDetacheRepository.save(pdp);

            // Enregistre le mouvement de stock
            pieceMouvementRepository.save(PieceMouvement.builder()
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
        }

        // Retire la ligne du BS
        bon.getLignesBonDeSortiePieces().remove(ligneARetirer);

        // Enregistre l'historique du BS
        bonDeSortieHistoriqueRepository.save(BonDeSortieHistorique.builder()
                .bonDeSortie(bon)
                .piece(piece)
                .quantite(-quantite)
                .stockMagasin(pdp != null ? pdp.getStockMagasin() : 0.0)
                .stockAtelier(pdp != null ? pdp.getStockAtelier() : 0.0)
                .qteReelle(pdp != null ? pdp.getQteReelle() : 0.0)
                .prenom(agentConnecte != null ? agentConnecte.getFirstName() : "")
                .nom(agentConnecte != null ? agentConnecte.getLastName() : "")
                .numBs(bon.getReference())
                .numeroSerie(piece != null ? piece.getReference() : "")
                .immatriculation(bon.getVehicule() != null ? bon.getVehicule().getImmatriculation() : "")
                .designation(piece != null ? piece.getDesignation() : "")
                .statut("RETOUR")
                .motif("Retour de la pièce " + (piece != null ? piece.getReference() : "") + " (" + (piece != null ? piece.getDesignation() : "") + ") - Qté: "
                        + (int) quantite)
                .agent(agentConnecte)
                .garage(bon.getGarage())
                .build());

        return bonDeSortieRepository.save(bon);
    }

    @Override
    public List<BonDeSortieHistorique> getHistorique(Long id) {
        return bonDeSortieHistoriqueRepository.findByBonDeSortieIdOrderByDateActionDesc(id);
    }

    @Override
    public Page<BonDeSortieHistorique> getHistorique(Long id, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateAction").descending());
        return bonDeSortieHistoriqueRepository.findByBonDeSortieId(id, pageable);
    }

    @Override
    public List<BonDeSortieHistorique> getAllHistorique() {
        return bonDeSortieHistoriqueRepository.findAllByOrderByDateActionDesc();
    }

    @Override
    public Page<BonDeSortieHistorique> getAllHistorique(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateAction").descending());
        return bonDeSortieHistoriqueRepository.findAll(pageable);
    }

    @Override
    public List<BonDeSortieHistorique> searchHistorique(String keyword) {
        return bonDeSortieHistoriqueRepository.searchHistorique(keyword);
    }

    @Override
    public Page<BonDeSortieHistorique> searchHistorique(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateAction").descending());
        return bonDeSortieHistoriqueRepository.searchHistorique(keyword, pageable);
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
    public Page<BonDeSortie> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return bonDeSortieRepository.findAll(pageable);
    }

    @Override
    public List<BonDeSortie> getByStatut(StatutBon statut) {
        return bonDeSortieRepository.findByStatutOrderByDateDesc(statut);
    }

    @Override
    public Page<BonDeSortie> getByStatut(StatutBon statut, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return bonDeSortieRepository.findByStatut(statut, pageable);
    }

    @Override
    public List<BonDeSortie> getByClient(Long clientId) {
        return bonDeSortieRepository.findByClientIdOrderByDateDesc(clientId);
    }

    @Override
    public Page<BonDeSortie> getByClient(Long clientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return bonDeSortieRepository.findByClientId(clientId, pageable);
    }

    @Override
    public List<BonDeSortie> getByVehicule(Long vehiculeId) {
        return bonDeSortieRepository.findByVehiculeIdOrderByDateDesc(vehiculeId);
    }

    @Override
    public Page<BonDeSortie> getByVehicule(Long vehiculeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return bonDeSortieRepository.findByVehiculeId(vehiculeId, pageable);
    }

    @Override
    public List<BonDeSortie> search(String keyword) {
        return bonDeSortieRepository.searchBonsDeSortie(keyword);
    }

    @Override
    public Page<BonDeSortie> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return bonDeSortieRepository.searchBonsDeSortie(keyword, pageable);
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

    private PDP resolvePDP(PieceDetache piece) {
        if (piece == null) return null;
        PieceDetache unproxied = (PieceDetache) org.hibernate.Hibernate.unproxy(piece);
        if (unproxied instanceof PDP pdp) {
            return pdp;
        }
        if (piece.getId() != null) {
            return pieceDetacheRepository.findById(piece.getId())
                    .map(p -> (PieceDetache) org.hibernate.Hibernate.unproxy(p))
                    .filter(PDP.class::isInstance)
                    .map(PDP.class::cast)
                    .orElse(null);
        }
        return null;
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
