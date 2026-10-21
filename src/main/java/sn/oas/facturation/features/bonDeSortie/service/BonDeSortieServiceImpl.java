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
import sn.oas.facturation.features.bonDeSortie.data.enums.TypeLigneBonDeSortie;
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
import sn.oas.facturation.features.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.features.main_doeuvre.repository.MainDoeuvreRepository;
import sn.oas.facturation.features.ordreTravail.data.entity.OrdreTravail;
import sn.oas.facturation.features.ordreTravail.data.enums.StatutOrdreTravail;
import sn.oas.facturation.features.ordreTravail.repository.OrdreTravailRepository;

import java.time.LocalDateTime;
import java.math.BigDecimal;
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
    private final MainDoeuvreRepository mainDoeuvreRepository;
    private final OrdreTravailRepository ordreTravailRepository;


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

        OrdreTravail ot = request.ordreTravailId() == null ? null : ordreTravailRepository.findById(request.ordreTravailId())
                .orElseThrow(() -> new IllegalArgumentException("Ordre de travail introuvable"));
        OrdreReparation fiche = request.ordreReparationId() != null
                ? ordreReparationRepository.findById(request.ordreReparationId())
                    .orElseThrow(() -> new IllegalArgumentException("Ordre de réparation introuvable"))
                : ot != null ? ot.getOrdreReparation() : ordreReparationRepository
                    .findFirstByVehiculeIdAndStatutNotIn(vehicule.getId(), List.of(StatutOrdreReparation.LIVRE))
                    .orElseThrow(() -> new IllegalArgumentException("Un ordre de réparation actif est obligatoire pour créer un bon de sortie"));
        if (fiche == null || fiche.getVehicule() == null || !fiche.getVehicule().getId().equals(vehicule.getId())) {
            throw new IllegalArgumentException("L'ordre de réparation ne correspond pas au véhicule sélectionné");
        }
        if (fiche.getClient() != null && !fiche.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("L'ordre de réparation ne correspond pas au client sélectionné");
        }
        if (ot != null && (ot.getVehicule() == null || !ot.getVehicule().getId().equals(vehicule.getId())
                || (ot.getOrdreReparation() != null && !ot.getOrdreReparation().getId().equals(fiche.getId())))) {
            throw new IllegalArgumentException("L'ordre de travail ne correspond pas à l'ordre de réparation sélectionné");
        }

        BonDeSortie bon = BonDeSortie.builder()
                .reference(genererReference())
                .client(client)
                .vehicule(vehicule)
                .agentEmetteur(agentEmetteur)
                .garage(agentEmetteur != null ? agentEmetteur.getGarage() : null)
                .remarque(request.remarque())
                .ordreReparation(fiche)
                .ordreTravail(ot)
                .build();

        if (request.lignesPieces() != null) {
            for (LignePieceRequest ligneReq : request.lignesPieces()) {
                if (ligneReq.quantite() == null || ligneReq.quantite() <= 0) {
                    throw new IllegalArgumentException("La quantité doit être supérieure à zéro");
                }
                String type = ligneReq.type() == null ? "PDP" : ligneReq.type().trim().toUpperCase();
                PieceDetache piece = null;
                MainDoeuvre mainDoeuvre = null;
                Double prixCatalogue = 0.0;
                TypeLigneBonDeSortie typeLigne = TypeLigneBonDeSortie.PIECE;
                String designation;
                if ("MAIN_DOEUVRE".equals(type) || "MO".equals(type)) {
                    typeLigne = TypeLigneBonDeSortie.MAIN_DOEUVRE;
                    if (ligneReq.mainDoeuvreId() == null) throw new IllegalArgumentException("La main-d'œuvre sélectionnée est obligatoire");
                    mainDoeuvre = mainDoeuvreRepository.findById(ligneReq.mainDoeuvreId())
                            .orElseThrow(() -> new IllegalArgumentException("Main-d'œuvre introuvable"));
                    if (Boolean.TRUE.equals(mainDoeuvre.getIsArchived())) throw new IllegalArgumentException("Cette main-d'œuvre est archivée");
                    prixCatalogue = mainDoeuvre.getPrix();
                    designation = mainDoeuvre.getDescription();
                } else {
                    if (ligneReq.pieceId() == null) {
                        if (!"PDS".equals(type) || ligneReq.designation() == null || ligneReq.designation().isBlank()) {
                            throw new IllegalArgumentException("Une pièce du catalogue ou une désignation PDS est obligatoire");
                        }
                        designation = ligneReq.designation().trim();
                    } else {
                        piece = pieceDetacheRepository.findById(ligneReq.pieceId())
                                .orElseThrow(() -> new IllegalArgumentException("Pièce introuvable avec l'id : " + ligneReq.pieceId()));
                        piece = (PieceDetache) org.hibernate.Hibernate.unproxy(piece);
                        if (!type.equals(piece.getType().name())) throw new IllegalArgumentException("Le type de pièce ne correspond pas à la pièce sélectionnée");
                        prixCatalogue = piece.getPrixUnitaire() != null ? piece.getPrixUnitaire() : 0.0;
                        designation = piece.getDesignation();
                    }
                }
                Double prix = ligneReq.getEffectivePrix() != null ? ligneReq.getEffectivePrix() : prixCatalogue;
                if (prix == null || prix < 0) throw new IllegalArgumentException("Le prix unitaire ne peut pas être négatif");
                LigneBonDeSortiePiece ligne = LigneBonDeSortiePiece.builder().bonDeSortie(bon)
                        .piece(piece).mainDoeuvre(mainDoeuvre).type(typeLigne).designation(designation)
                        .quantite(ligneReq.quantite()).prix((int) Math.round(prix)).build();
                bon.getLignesBonDeSortiePieces().add(ligne);
            }
        }

        recalculerTotaux(bon);

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
                PieceDetache piece = ligne.getPiece() == null ? null : (PieceDetache) org.hibernate.Hibernate.unproxy(ligne.getPiece());
                String designation = ligne.getDesignation() != null ? ligne.getDesignation()
                        : piece != null ? piece.getDesignation()
                        : ligne.getMainDoeuvre() != null ? ligne.getMainDoeuvre().getDescription() : "";
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
                        .designation(designation)
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
            fiche.addBonDeSortie(saved);
            ordreReparationRepository.save(fiche);
            agentNotificationService.notifyRoleInGarage(Role.AGENT_MAGASIN, saved.getGarage(),
                    "Nouveau Bon de Sortie",
                    "Le bon de sortie " + saved.getReference() + " est en attente de validation pour la fiche "
                            + fiche.getNumero());
        }
        if (saved.getOrdreTravail() != null) {
            OrdreTravail ordreTravail = saved.getOrdreTravail();
            ordreTravail.addBonDeSortie(saved);
            ordreTravailRepository.save(ordreTravail);
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
        OrdreReparation ordre = bon.getOrdreReparation();
        OrdreTravail ordreTravail = bon.getOrdreTravail();
        if (ordreTravail == null && ordre != null) {
            ordreTravail = ordreTravailRepository.findByOrdreReparationId(ordre.getId()).orElse(null);
        }
        boolean reparationTerminee = ordreTravail != null
                ? ordreTravail.getStatut() == StatutOrdreTravail.TERMINE
                : ordre != null && (ordre.getStatut() == StatutOrdreReparation.PAIEMENT
                        || ordre.getStatut() == StatutOrdreReparation.PRET_A_LIVRER
                        || ordre.getStatut() == StatutOrdreReparation.LIVRE);
        if (!reparationTerminee) {
            throw new IllegalStateException("Le bon de sortie ne peut être validé qu'après la fin de la réparation");
        }

        // A la validation du BS : stockAtelier diminue (la pièce va sur le véhicule),
        // qteReelle reste le même
        for (LigneBonDeSortiePiece ligne : bon.getLignesBonDeSortiePieces()) {
            if (ligne.getPiece() == null) continue;
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
                PieceDetache piece = ligne.getPiece() == null ? null : (PieceDetache) org.hibernate.Hibernate.unproxy(ligne.getPiece());
                String designation = ligne.getDesignation() != null ? ligne.getDesignation() : piece != null ? piece.getDesignation() : ligne.getMainDoeuvre() != null ? ligne.getMainDoeuvre().getDescription() : "";
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
                        .designation(designation)
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
            if (fiche.getStatut() == StatutOrdreReparation.PROFORMA) {
                fiche.setStatut(StatutOrdreReparation.ORDRE_DE_TRAVAIL);
                agentNotificationService.notifyRoleInGarage(Role.CHEF_ATELIER, bon.getGarage(),
                        "Bon de sortie validé",
                        "Le bon de sortie " + bon.getReference() + " a été validé. La fiche " + fiche.getNumero()
                                + " est en cours d'exécution.");
                ordreReparationRepository.save(fiche);
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
            if (l.getPiece() != null && l.getPiece().getId().equals(pieceId)) {
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
        recalculerTotaux(bon);

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
    @Transactional(readOnly = true)
    public Page<BonDeSortie> getByOrdreReparation(Long ordreReparationId, int page, int size) {
        return bonDeSortieRepository.findByOrdreReparationIdOrderByDateDesc(ordreReparationId, PageRequest.of(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BonDeSortie> getByOrdreTravail(Long ordreTravailId, int page, int size) {
        return bonDeSortieRepository.findByOrdreTravailIdOrderByDateDesc(ordreTravailId, PageRequest.of(page, size));
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
        if (request.lignesPieces() == null || request.lignesPieces().isEmpty()) {
            throw new IllegalArgumentException("Le bon de sortie doit contenir au moins une ligne");
        }
    }

    private void recalculerTotaux(BonDeSortie bon) {
        BigDecimal totalPieces = BigDecimal.ZERO;
        BigDecimal totalMo = BigDecimal.ZERO;
        for (LigneBonDeSortiePiece ligne : bon.getLignesBonDeSortiePieces()) {
            BigDecimal totalLigne = BigDecimal.valueOf(ligne.getPrix()).multiply(BigDecimal.valueOf(ligne.getQuantite()));
            if (ligne.getType() == TypeLigneBonDeSortie.MAIN_DOEUVRE) totalMo = totalMo.add(totalLigne);
            else totalPieces = totalPieces.add(totalLigne);
        }

        bon.setMontantTotalPieces(totalPieces);
        bon.setMontantTotalMainDoeuvre(totalMo);
        bon.setMontantTotal(totalPieces.add(totalMo));
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
