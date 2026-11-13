package sn.oas.facturation.features.noteDePrix.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.oas.facturation.features.user.repository.UserRepository;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationMainDoeuvre;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationPiece;
import sn.oas.facturation.features.facturation.data.enums.StatutFacturation;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;
import sn.oas.facturation.features.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.features.main_doeuvre.repository.MainDoeuvreRepository;
import sn.oas.facturation.features.piecedetache.data.entity.PieceMouvement;
import sn.oas.facturation.features.piecedetache.data.enums.TypeMouvement;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.features.noteDePrix.dto.NoteDePrixRequest;
import sn.oas.facturation.features.noteDePrix.repository.NoteDePrixRepository;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.repository.PDPRepository;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.garage.repository.GarageRepository;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.documentNumber.DocumentType;
import sn.oas.facturation.features.piecedetache.repository.PieceMouvementRepository;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.facture.service.FacturePaymentMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteDePrixServiceImpl implements NoteDePrixService {

    private final NoteDePrixRepository noteDePrixRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final PDPRepository pdpRepository;
    private final PieceDetacheRepository pieceDetacheRepository;
    private final MainDoeuvreRepository mainDoeuvreRepository;
    private final ClientRepository clientRepository;
    private final VehiculeRepository vehiculeRepository;
    private final UserRepository userRepository;
    private final DocumentNumberGeneratorService documentNumberGeneratorService;
    private final PieceMouvementRepository pieceMouvementRepository;
    private final GarageRepository garageRepository;
    private final sn.oas.facturation.features.fiscal.service.FiscalConfigurationService fiscalConfigurationService;

    private Agent getAgentConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            User user = userRepository.findByUsername(auth.getName())
                    .or(() -> userRepository.findByEmail(auth.getName()))
                    .orElse(null);
            return (user instanceof Agent) ? (Agent) user : null;
        }
        return null;
    }

    private Garage getGarageConnecte(Agent agent) {
        if (agent != null && agent.getGarage() != null) {
            return agent.getGarage();
        }
        org.springframework.web.context.request.ServletRequestAttributes attributes = 
                (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String garageIdHeader = attributes.getRequest().getHeader("X-Garage-ID");
            if (garageIdHeader != null && !garageIdHeader.isEmpty()) {
                try {
                    Garage g = garageRepository.findById(Long.parseLong(garageIdHeader)).orElse(null);
                    if (g != null) return g;
                } catch (Exception ignored) {}
            }
        }
        Garage g = documentNumberGeneratorService.getCurrentGarage();
        if (g != null) return g;
        return garageRepository.findAll().stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public NoteDePrix createNoteDePrix(NoteDePrixRequest request) {
        OrdreReparation fiche = null;
        if (request.getOrdreReparationId() != null) {
            fiche = ordreReparationRepository.findById(request.getOrdreReparationId()).orElse(null);
        }

        Agent agentConnecte = getAgentConnecte();
        Garage garage = (fiche != null && fiche.getGarage() != null) ? fiche.getGarage() : getGarageConnecte(agentConnecte);

        NoteDePrix note = NoteDePrix.builder()
                .numero(documentNumberGeneratorService.generateNextNumber(garage, DocumentType.NP))
                .ordreReparation(fiche)
                .agent(agentConnecte)
                .garage(garage)
                .kilometrage(request.getKilometrage())
                .remarque(request.getRemarque())
                .statut(StatutFacturation.EN_ATTENTE)
                .lignesFacturationPieces(new ArrayList<>())
                .lignesFacturationMainDoeuvres(new ArrayList<>())
                .build();

        BigDecimal montantTotalHT = BigDecimal.ZERO;

        if (request.getLignesPieces() != null && !request.getLignesPieces().isEmpty()) {
            for (var lp : request.getLignesPieces()) {
                PDP piece = null;
                if (lp.getPieceId() != null) {
                    PieceDetache pd = pieceDetacheRepository.findById(lp.getPieceId()).orElse(null);
                    if (pd instanceof PDP p) {
                        piece = p;
                    }
                }
                LigneFacturationPiece ligne = LigneFacturationPiece.builder()
                        .facturation(note)
                        .piece(piece)
                        .isCustom(piece == null)
                        .designationPds(piece != null ? piece.getDesignation() : lp.getDesignationPds())
                        .quantite(lp.getQuantite() != null ? lp.getQuantite() : 1)
                        .prix(lp.getPrix() != null ? lp.getPrix() : 0)
                        .build();
                int ligneTotal = ligne.getPrix() * ligne.getQuantite();
                montantTotalHT = montantTotalHT.add(BigDecimal.valueOf(ligneTotal));
                note.getLignesFacturationPieces().add(ligne);
            }
        } else if (fiche != null && fiche.getLignesOrdreReparationPieces() != null) {
            for (var lf : fiche.getLignesOrdreReparationPieces()) {
                LigneFacturationPiece ligne = LigneFacturationPiece.builder()
                        .facturation(note)
                        .piece(lf.getPiece())
                        .isCustom(lf.getPiece() == null)
                        .designationPds(lf.getPiece() != null ? lf.getPiece().getDesignation() : lf.getDesignationPds())
                        .quantite(lf.getQuantite())
                        .prix(lf.getPrix())
                        .build();
                int ligneTotal = ligne.getPrix() * ligne.getQuantite();
                montantTotalHT = montantTotalHT.add(BigDecimal.valueOf(ligneTotal));
                note.getLignesFacturationPieces().add(ligne);
            }
        }

        if (request.getLignesMainDoeuvres() != null && !request.getLignesMainDoeuvres().isEmpty()) {
            for (var lm : request.getLignesMainDoeuvres()) {
                MainDoeuvre md = null;
                if (lm.getMainDoeuvreId() != null) {
                    md = mainDoeuvreRepository.findById(lm.getMainDoeuvreId()).orElse(null);
                }
                LigneFacturationMainDoeuvre ligne = LigneFacturationMainDoeuvre.builder()
                        .facturation(note)
                        .mainDoeuvre(md)
                        .nbreHeure(lm.getNbreHeure() != null ? lm.getNbreHeure() : 1)
                        .tarifHoraire(lm.getTarifHoraire() != null ? lm.getTarifHoraire() : 0)
                        .build();
                int ligneTotal = ligne.getNbreHeure() * ligne.getTarifHoraire();
                montantTotalHT = montantTotalHT.add(BigDecimal.valueOf(ligneTotal));
                note.getLignesFacturationMainDoeuvres().add(ligne);
            }
        } else if (fiche != null && fiche.getLignesOrdreReparationMainDoeuvres() != null) {
            for (var lf : fiche.getLignesOrdreReparationMainDoeuvres()) {
                LigneFacturationMainDoeuvre ligne = LigneFacturationMainDoeuvre.builder()
                        .facturation(note)
                        .mainDoeuvre(lf.getMainDoeuvre())
                        .nbreHeure(lf.getNbreHeure())
                        .tarifHoraire(lf.getPrix())
                        .build();
                int ligneTotal = ligne.getNbreHeure() * ligne.getTarifHoraire();
                montantTotalHT = montantTotalHT.add(BigDecimal.valueOf(ligneTotal));
                note.getLignesFacturationMainDoeuvres().add(ligne);
            }
        }

        Client client = null;
        if (request.getClientId() != null) {
            client = clientRepository.findById(request.getClientId()).orElse(null);
        } else if (fiche != null && fiche.getVehicule() != null) {
            client = fiche.getVehicule().getClient();
        }

        Vehicule vehicule = null;
        if (request.getVehiculeId() != null) {
            vehicule = vehiculeRepository.findById(request.getVehiculeId()).orElse(null);
        } else if (fiche != null) {
            vehicule = fiche.getVehicule();
        }
        sn.oas.facturation.features.vehicule.service.VehiculeActivationPolicy.requireActive(vehicule);

        if (client == null && vehicule != null) {
            client = vehicule.getClient();
        }

        note.setClient(client);
        note.setVehicule(vehicule);
        String modePaiement = FacturePaymentMode.normalize(request.getModePaiement());
        note.setModePaiement(modePaiement);
        note.setNumeroBonDeCommande(request.getNumeroBonDeCommande());
        note.setMontantAutre(request.getMontantAutre() != null ? request.getMontantAutre() : BigDecimal.ZERO);
        BigDecimal totalFinal = montantTotalHT.add(note.getMontantAutre());
        boolean isHt = FacturePaymentMode.isHt(modePaiement);
        boolean appliquerTVA = !isHt && Boolean.TRUE.equals(request.getAppliquerTVA());
        BigDecimal montantTVA = appliquerTVA
                ? fiscalConfigurationService.calculateRate(totalFinal, fiscalConfigurationService.getCurrent().getTvaRate())
                : BigDecimal.ZERO;
        BigDecimal timbre = isHt
                ? fiscalConfigurationService.calculateRate(totalFinal, fiscalConfigurationService.getCurrent().getTimbreFiscalRate())
                : BigDecimal.ZERO;
        note.setTvaApplicable(appliquerTVA);
        note.setMontantTVA(montantTVA);
        note.setMontantHT(totalFinal);
        BigDecimal totalDocument = totalFinal.add(montantTVA).add(timbre);
        note.setMontantTotal(totalDocument);
        note.setMontantPaye(BigDecimal.ZERO);
        note.setMontantTimbrePaye(timbre);
        note.setResteAPayer(totalDocument);
        note.setStatutPaiement(StatutPaiement.NON_PAYE);

        NoteDePrix saved = noteDePrixRepository.save(note);
        // Mouvement de stock : La Note de prix diminue le stock réel (SORTIE RÉELLE)
        for (LigneFacturationPiece lfp : saved.getLignesFacturationPieces()) {
            if (lfp.getPiece() != null && lfp.getPiece() instanceof PDP pdp) {
                double qteReelleAvant = pdp.getQteReelle() != null ? pdp.getQteReelle()
                        : (pdp.getStockMagasin() + pdp.getStockAtelier());
                pdp.setQteReelle(Math.max(0.0, qteReelleAvant - lfp.getQuantite()));
                pdpRepository.save(pdp);

                pieceMouvementRepository.save(PieceMouvement.builder()
                        .type(TypeMouvement.SORTIE_REELLE)
                        .quantite((double) lfp.getQuantite())
                        .stockMagasinAvant(pdp.getStockMagasin())
                        .stockAtelierAvant(pdp.getStockAtelier())
                        .stockMagasinApres(pdp.getStockMagasin())
                        .stockAtelierApres(pdp.getStockAtelier())
                        .stockReelApres(pdp.getQteReelle())
                        .prenom(agentConnecte != null ? agentConnecte.getFirstName() : "")
                        .nom(agentConnecte != null ? agentConnecte.getLastName() : "")
                        .numDocument(saved.getNumero())
                        .typeDocument("Note de prix")
                        .numeroSerie(pdp.getReference())
                        .immatriculation(fiche != null && fiche.getVehicule() != null ? fiche.getVehicule().getImmatriculation() : (vehicule != null ? vehicule.getImmatriculation() : ""))
                        .motif("Note de prix " + saved.getNumero())
                        .piece(pdp)
                        .agent(agentConnecte)
                        .garage(saved.getGarage())
                        .build());
            }
        }
        return saved;
    }

    @Override
    @Transactional
    public NoteDePrix updateNoteDePrix(Long id, NoteDePrixRequest request) {
        NoteDePrix note = noteDePrixRepository.findById(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Note de prix non trouvée avec l'id : " + id));

        if (request.getOrdreReparationId() != null) {
            note.setOrdreReparation(ordreReparationRepository.findById(request.getOrdreReparationId())
                    .orElse(note.getOrdreReparation()));
        }
        if (request.getKilometrage() != null) {
            note.setKilometrage(request.getKilometrage());
        }
        if (request.getRemarque() != null) {
            note.setRemarque(request.getRemarque());
        }
        if (request.getClientId() != null) {
            clientRepository.findById(request.getClientId()).ifPresent(note::setClient);
        }
        if (request.getVehiculeId() != null) {
            vehiculeRepository.findById(request.getVehiculeId()).ifPresent(vehicule -> {
                sn.oas.facturation.features.vehicule.service.VehiculeActivationPolicy.requireActive(vehicule);
                note.setVehicule(vehicule);
            });
        } else if (note.getVehicule() != null) {
            sn.oas.facturation.features.vehicule.service.VehiculeActivationPolicy.requireActive(note.getVehicule());
        }
        if (request.getModePaiement() != null) {
            note.setModePaiement(request.getModePaiement());
        }
        if (request.getNumeroBonDeCommande() != null) {
            note.setNumeroBonDeCommande(request.getNumeroBonDeCommande());
        }

        if (request.getLignesPieces() != null) {
            note.getLignesFacturationPieces().clear();
            for (var lp : request.getLignesPieces()) {
                PDP piece = null;
                if (lp.getPieceId() != null) {
                    PieceDetache pd = pieceDetacheRepository.findById(lp.getPieceId()).orElse(null);
                    if (pd instanceof PDP p) {
                        piece = p;
                    }
                }
                LigneFacturationPiece ligne = LigneFacturationPiece.builder()
                        .facturation(note)
                        .piece(piece)
                        .isCustom(piece == null)
                        .designationPds(piece != null ? piece.getDesignation() : lp.getDesignationPds())
                        .quantite(lp.getQuantite() != null ? lp.getQuantite() : 1)
                        .prix(lp.getPrix() != null ? lp.getPrix() : 0)
                        .build();
                note.getLignesFacturationPieces().add(ligne);
            }
        }

        if (request.getLignesMainDoeuvres() != null) {
            note.getLignesFacturationMainDoeuvres().clear();
            for (var lm : request.getLignesMainDoeuvres()) {
                MainDoeuvre md = null;
                if (lm.getMainDoeuvreId() != null) {
                    md = mainDoeuvreRepository.findById(lm.getMainDoeuvreId()).orElse(null);
                }
                LigneFacturationMainDoeuvre ligne = LigneFacturationMainDoeuvre.builder()
                        .facturation(note)
                        .mainDoeuvre(md)
                        .nbreHeure(lm.getNbreHeure() != null ? lm.getNbreHeure() : 1)
                        .tarifHoraire(lm.getTarifHoraire() != null ? lm.getTarifHoraire() : 0)
                        .build();
                note.getLignesFacturationMainDoeuvres().add(ligne);
            }
        }

        if (request.getMontantAutre() != null) {
            note.setMontantAutre(request.getMontantAutre());
        }

        // Recalculate totals if lines, montantAutre, TVA or modePaiement were updated
        if (request.getModePaiement() != null) {
            note.setModePaiement(FacturePaymentMode.normalize(request.getModePaiement()));
        }
        if (request.getAppliquerTVA() != null) note.setTvaApplicable(request.getAppliquerTVA());
        if (request.getLignesPieces() != null || request.getLignesMainDoeuvres() != null || request.getMontantAutre() != null
                || request.getAppliquerTVA() != null || request.getModePaiement() != null) {
            BigDecimal piecesSum = note.getLignesFacturationPieces().stream()
                    .map(l -> BigDecimal.valueOf((long) (l.getPrix() != null ? l.getPrix() : 0) * (l.getQuantite() != null ? l.getQuantite() : 0)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal moSum = note.getLignesFacturationMainDoeuvres().stream()
                    .map(l -> BigDecimal.valueOf((long) (l.getTarifHoraire() != null ? l.getTarifHoraire() : 0) * (l.getNbreHeure() != null ? l.getNbreHeure() : 0)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalHT = piecesSum.add(moSum).add(note.getMontantAutre() != null ? note.getMontantAutre() : BigDecimal.ZERO);
            boolean isHt = FacturePaymentMode.isHt(note.getModePaiement());
            boolean appliquerTVA = !isHt && Boolean.TRUE.equals(note.getTvaApplicable());
            BigDecimal montantTVA = appliquerTVA
                    ? fiscalConfigurationService.calculateRate(totalHT, fiscalConfigurationService.getCurrent().getTvaRate())
                    : BigDecimal.ZERO;
            BigDecimal timbre = isHt
                    ? fiscalConfigurationService.calculateRate(totalHT, fiscalConfigurationService.getCurrent().getTimbreFiscalRate())
                    : BigDecimal.ZERO;
            BigDecimal totalDocument = totalHT.add(montantTVA).add(timbre);
            note.setMontantHT(totalHT);
            note.setMontantTVA(montantTVA);
            note.setMontantTotal(totalDocument);
            if (isHt) {
                note.setMontantTimbrePaye(timbre);
            }
            BigDecimal paye = note.getMontantPaye() != null ? note.getMontantPaye() : BigDecimal.ZERO;
            BigDecimal reste = totalDocument.subtract(paye).max(BigDecimal.ZERO);
            note.setResteAPayer(reste);
            if (reste.compareTo(BigDecimal.ZERO) == 0 && totalDocument.compareTo(BigDecimal.ZERO) > 0) {
                note.setStatutPaiement(StatutPaiement.PAYE);
            } else if (paye.compareTo(BigDecimal.ZERO) > 0) {
                note.setStatutPaiement(StatutPaiement.PARTIEL);
            } else {
                note.setStatutPaiement(StatutPaiement.NON_PAYE);
            }
        }

        if (request.getMontantPaye() != null) {
            BigDecimal montantPayeActuel = note.getMontantPaye() != null ? note.getMontantPaye() : BigDecimal.ZERO;
            BigDecimal nouveauMontantPaye = request.getMontantPaye();
            if (nouveauMontantPaye.compareTo(montantPayeActuel) < 0) {
                throw new sn.oas.facturation.shared.exception.BadRequestException(
                        "Le montant payé d'une note de prix ne peut pas être diminué.");
            }
            if (nouveauMontantPaye.compareTo(note.getMontantTotal()) > 0) {
                throw new sn.oas.facturation.shared.exception.BadRequestException(
                        "Le montant payé dépasse le total de la note de prix.");
            }
            BigDecimal partNouvellementPayee = nouveauMontantPaye.subtract(montantPayeActuel);
            String modePaiementEffectif = request.getModePaiement() != null
                    ? request.getModePaiement().trim().toUpperCase(java.util.Locale.ROOT)
                    : note.getModePaiement();
            boolean isEspeces = "ESPECE".equals(modePaiementEffectif) || "ESPECE_TTC".equals(modePaiementEffectif);
            boolean timbreDejaInclus = FacturePaymentMode.isHt(note.getModePaiement());
            if (isEspeces && !timbreDejaInclus && partNouvellementPayee.signum() > 0) {
                BigDecimal timbre = fiscalConfigurationService.calculateRate(partNouvellementPayee,
                        fiscalConfigurationService.getCurrent().getTimbreFiscalRate());
                BigDecimal timbreActuel = note.getMontantTimbrePaye() != null ? note.getMontantTimbrePaye() : BigDecimal.ZERO;
                note.setMontantTimbrePaye(timbreActuel.add(timbre));
            }
            note.setMontantPaye(request.getMontantPaye());
            BigDecimal total = note.getMontantTotal() != null ? note.getMontantTotal() : BigDecimal.ZERO;
            BigDecimal reste = total.subtract(request.getMontantPaye()).max(BigDecimal.ZERO);
            note.setResteAPayer(reste);
            if (reste.compareTo(BigDecimal.ZERO) == 0 && total.compareTo(BigDecimal.ZERO) > 0) {
                note.setStatutPaiement(StatutPaiement.PAYE);
            } else if (request.getMontantPaye().compareTo(BigDecimal.ZERO) > 0) {
                note.setStatutPaiement(StatutPaiement.PARTIEL);
            } else {
                note.setStatutPaiement(StatutPaiement.NON_PAYE);
            }
        }

        return noteDePrixRepository.save(note);
    }

    @Override
    @Transactional(readOnly = true)
    public NoteDePrix getNoteDePrix(Long id) {
        return noteDePrixRepository.findById(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Note de prix non trouvée avec l'id : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteDePrix> getAllNotesDePrix() {
        return noteDePrixRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteDePrix> getAllNotesDePrix(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return noteDePrixRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteDePrix> getAllFiltered(
            String keyword,
            String numero,
            String immatriculation,
            Long clientId,
            String clientNumero,
            String clientMatricule,
            String clientKeyword,
            String statut,
            java.time.LocalDate dateDebut,
            java.time.LocalDate dateFin,
            Long ordreReparationId,
            int page,
            int size) {

        int maxPageSize = 1000;
        int effectivePage = Math.max(0, page);
        int effectiveSize = size > 0 ? Math.min(size, maxPageSize) : 10;
        Pageable pageable = PageRequest.of(effectivePage, effectiveSize, Sort.by(Sort.Direction.DESC, "dateCreation"));

        java.time.LocalDateTime debut = dateDebut == null ? null : dateDebut.atStartOfDay();
        java.time.LocalDateTime finExclusive = dateFin == null ? null : dateFin.plusDays(1).atStartOfDay();

        String motCle = (keyword == null || keyword.isBlank()) ? null : keyword.trim().toLowerCase();
        String numFiltre = (numero == null || numero.isBlank()) ? null : numero.trim().toLowerCase();
        String immatFiltre = (immatriculation == null || immatriculation.isBlank()) ? null : immatriculation.trim().toLowerCase();

        String matriculeFiltre = clientNumero != null && !clientNumero.isBlank()
                ? clientNumero.trim()
                : (clientMatricule != null && !clientMatricule.isBlank() ? clientMatricule.trim() : null);

        Long clientIdFilter = clientId;
        String clientTexte = clientKeyword != null && !clientKeyword.isBlank() ? clientKeyword.trim() : null;

        if (clientIdFilter == null && matriculeFiltre != null && matriculeFiltre.matches("^\\d+$")) {
            try {
                clientIdFilter = Long.valueOf(matriculeFiltre);
            } catch (NumberFormatException ignored) {}
        }

        if (clientIdFilter == null && clientTexte != null) {
            java.util.regex.Matcher idClient = java.util.regex.Pattern
                    .compile("(?i)^client\\s*#?\\s*(\\d+)$|^#?(\\d+)$")
                    .matcher(clientTexte);
            if (idClient.matches()) {
                String digits = idClient.group(1) != null ? idClient.group(1) : idClient.group(2);
                try {
                    clientIdFilter = Long.valueOf(digits);
                    clientTexte = null;
                } catch (NumberFormatException ignored) {}
            }
        }

        final Long finalClientIdFilter = clientIdFilter;
        final String finalClientTexte = clientTexte;
        final String finalMatriculeFiltre = matriculeFiltre;

        org.springframework.data.jpa.domain.Specification<NoteDePrix> specification = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            var clientDirect = root.join("client", jakarta.persistence.criteria.JoinType.LEFT);
            var vehiculeDirect = root.join("vehicule", jakarta.persistence.criteria.JoinType.LEFT);
            var ordre = root.join("ordreReparation", jakarta.persistence.criteria.JoinType.LEFT);
            var clientOrdre = ordre.join("client", jakarta.persistence.criteria.JoinType.LEFT);
            var vehiculeOrdre = ordre.join("vehicule", jakarta.persistence.criteria.JoinType.LEFT);
            var clientVehiculeOrdre = vehiculeOrdre.join("client", jakarta.persistence.criteria.JoinType.LEFT);

            jakarta.persistence.criteria.Expression<String> immatriculationExpr = coalesceText(cb,
                    vehiculeDirect.<String>get("immatriculation"), vehiculeOrdre.<String>get("immatriculation"));
            jakarta.persistence.criteria.Expression<String> marque = coalesceText(cb,
                    vehiculeDirect.<String>get("marque"), vehiculeOrdre.<String>get("marque"));
            jakarta.persistence.criteria.Expression<String> modele = coalesceText(cb,
                    vehiculeDirect.<String>get("modele"), vehiculeOrdre.<String>get("modele"));
            jakarta.persistence.criteria.Expression<String> numeroChassis = coalesceText(cb,
                    vehiculeDirect.<String>get("numeroChassis"), vehiculeOrdre.<String>get("numeroChassis"));

            jakarta.persistence.criteria.Expression<String> matriculeClient = coalesceText(cb,
                    clientDirect.<String>get("matricule"), clientOrdre.<String>get("matricule"),
                    clientVehiculeOrdre.<String>get("matricule"));

            jakarta.persistence.criteria.Expression<String> prenom = coalesceText(cb,
                    clientDirect.<String>get("firstName"), clientOrdre.<String>get("firstName"),
                    clientVehiculeOrdre.<String>get("firstName"));
            jakarta.persistence.criteria.Expression<String> nom = coalesceText(cb,
                    clientDirect.<String>get("lastName"), clientOrdre.<String>get("lastName"),
                    clientVehiculeOrdre.<String>get("lastName"));
            jakarta.persistence.criteria.Expression<String> nomComplet = cb.concat(cb.concat(prenom, " "), nom);

            if (motCle != null) {
                String pattern = "%" + motCle + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(cb.coalesce(root.<String>get("numero"), "")), pattern),
                        cb.like(cb.lower(cb.coalesce(root.<String>get("remarque"), "")), pattern),
                        cb.like(cb.lower(cb.coalesce(root.<String>get("numeroBonDeCommande"), "")), pattern),
                        cb.like(cb.lower(cb.coalesce(ordre.<String>get("numero"), "")), pattern),
                        cb.like(cb.lower(immatriculationExpr), pattern),
                        cb.like(cb.lower(numeroChassis), pattern),
                        cb.like(cb.lower(marque), pattern),
                        cb.like(cb.lower(modele), pattern),
                        cb.like(cb.lower(matriculeClient), pattern),
                        cb.like(cb.lower(prenom), pattern),
                        cb.like(cb.lower(nom), pattern),
                        cb.like(cb.lower(nomComplet), pattern)));
            }

            if (numFiltre != null) {
                predicates.add(cb.like(cb.lower(cb.coalesce(root.<String>get("numero"), "")), "%" + numFiltre + "%"));
            }

            if (immatFiltre != null) {
                predicates.add(cb.like(cb.lower(immatriculationExpr), "%" + immatFiltre + "%"));
            }

            if (finalMatriculeFiltre != null) {
                predicates.add(cb.like(cb.lower(matriculeClient), "%" + finalMatriculeFiltre.toLowerCase() + "%"));
            }

            if (finalClientIdFilter != null) {
                var idClientEffectif = cb.coalesce(clientDirect.<Long>get("id"), clientOrdre.<Long>get("id"));
                predicates.add(cb.equal(cb.coalesce(idClientEffectif, clientVehiculeOrdre.<Long>get("id")), finalClientIdFilter));
            }

            if (finalClientTexte != null) {
                String pattern = "%" + finalClientTexte.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(matriculeClient), pattern),
                        cb.like(cb.lower(prenom), pattern),
                        cb.like(cb.lower(nom), pattern),
                        cb.like(cb.lower(nomComplet), pattern)));
            }

            if (statut != null && !statut.isBlank()) {
                String st = statut.trim().toUpperCase();
                if ("PAYE".equals(st)) {
                    predicates.add(cb.equal(root.get("statutPaiement"), sn.oas.facturation.features.facture.data.enums.StatutPaiement.PAYE));
                } else if ("EN_COURS".equals(st)) {
                    predicates.add(cb.or(root.get("statutPaiement").isNull(),
                            cb.notEqual(root.get("statutPaiement"), sn.oas.facturation.features.facture.data.enums.StatutPaiement.PAYE)));
                } else if ("NON_PAYE".equals(st)) {
                    predicates.add(cb.equal(root.get("statutPaiement"), sn.oas.facturation.features.facture.data.enums.StatutPaiement.NON_PAYE));
                } else if ("PARTIEL".equals(st)) {
                    predicates.add(cb.equal(root.get("statutPaiement"), sn.oas.facturation.features.facture.data.enums.StatutPaiement.PARTIEL));
                }
            }

            if (debut != null) predicates.add(cb.greaterThanOrEqualTo(root.get("dateCreation"), debut));
            if (finExclusive != null) predicates.add(cb.lessThan(root.get("dateCreation"), finExclusive));
            if (ordreReparationId != null) predicates.add(cb.equal(ordre.<Long>get("id"), ordreReparationId));

            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return noteDePrixRepository.findAll(specification, pageable);
    }

    @SafeVarargs
    private static jakarta.persistence.criteria.Expression<String> coalesceText(
            jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Expression<String>... expressions) {
        jakarta.persistence.criteria.Expression<String> result = cb.coalesce(expressions[0], "");
        for (int i = 1; i < expressions.length; i++) result = cb.coalesce(result, expressions[i]);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteDePrix> searchNotesDePrix(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return noteDePrixRepository.searchNotesDePrix(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteDePrix> searchNotesDePrix(String keyword) {
        return noteDePrixRepository.searchNotesDePrix(keyword);
    }

    @Override
    @Transactional
    public void deleteNoteDePrix(Long id) {
        if (!noteDePrixRepository.existsById(id)) {
            throw new sn.oas.facturation.shared.exception.ResourceNotFoundException("Note de prix non trouvée avec l'id : " + id);
        }
        noteDePrixRepository.deleteById(id);
    }
}
