package sn.oas.facturation.features.recu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.client.service.PortefeuilleClientService;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.features.noteDePrix.repository.NoteDePrixRepository;
import sn.oas.facturation.features.notification.service.AgentNotificationService;
import sn.oas.facturation.features.notification.service.NotificationService;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.pdfGenerator.service.PdfLayout;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.recu.data.entity.LigneVersementClient;
import sn.oas.facturation.features.recu.data.entity.RecuFacture;
import sn.oas.facturation.features.recu.data.entity.Recu;
import sn.oas.facturation.features.recu.data.entity.VersementClient;
import sn.oas.facturation.features.recu.dto.DocumentRecuDto;
import sn.oas.facturation.features.recu.dto.RecuDetailResponse;
import sn.oas.facturation.features.recu.dto.RecuListItemResponse;
import sn.oas.facturation.features.recu.dto.VersementClientDto;
import sn.oas.facturation.features.recu.dto.VersementClientRequest;
import sn.oas.facturation.features.recu.repository.RecuRepository;
import sn.oas.facturation.features.recu.repository.RecuFactureRepository;
import sn.oas.facturation.features.recu.repository.VersementClientRepository;
import sn.oas.facturation.features.user.data.enums.Role;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.documentNumber.DocumentType;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Reçus clients, repris de l'ancienne application (add_recu_facture, recu-encours, recu-payees).
 * <p>
 * Un client a au plus un reçu en cours, qui couvre toutes ses factures et notes de prix impayées.
 * Un versement est réparti sur ces documents, des plus anciens aux plus récents. Sur une facture, un
 * {@link RecuFacture} (numéro RC) est créé et la facture, le solde prépayé et l'ordre de réparation sont mis à jour.
 * Sur une note de prix, son montant payé est mis à jour. Le solde prépayé ne règle que des factures.
 * Quand tout est payé, le reçu est clôturé et passe dans la liste des reçus payés, où il peut être validé.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecuServiceImpl implements RecuService {

    static final String MODE_PAR_DEFAUT = "ESPECE";

    private final ClientRepository clientRepository;
    private final FactureRepository factureRepository;
    private final NoteDePrixRepository noteDePrixRepository;
    private final RecuFactureRepository recuFactureRepository;
    private final RecuRepository recuRepository;
    private final VersementClientRepository versementClientRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final DocumentNumberGeneratorService documentNumberGeneratorService;
    private final NotificationService notificationService;
    private final AgentNotificationService agentNotificationService;
    private final PortefeuilleClientService portefeuilleClientService;

    // ---------------------------------------------------------------- reçu

    @Override
    @Transactional(noRollbackFor = BadRequestException.class) // garde la suppression d'un reçu vide
    public RecuDetailResponse ouvrirRecu(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable avec l'id : " + clientId));
        BigDecimal reste = resteClient(clientId);
        Recu recu = recuRepository.findFirstByClientIdAndDateFinIsNullOrderByIdDesc(clientId)
                .orElse(null);
        if (recu == null) {
            if (reste.signum() <= 0) {
                throw new BadRequestException("Ce client n'a aucune facture ni note de prix à régler.");
            }
            recu = Recu.builder()
                    .client(client)
                    .numero(documentNumberGeneratorService.generateNextNumber(DocumentType.RE))
                    .build();
        } else if (reste.signum() <= 0) {
            // Plus rien à régler : un reçu déjà payé en partie est clôturé, un reçu vide est supprimé.
            if (recu.getMontantPaye().signum() <= 0) {
                recuRepository.delete(recu);
                throw new BadRequestException("Ce client n'a aucune facture ni note de prix à régler.");
            }
            recu.setDateFin(LocalDateTime.now());
        }
        if (recu.getNumero() == null) {
            // Reçu créé avant la numérotation RE.
            recu.setNumero(documentNumberGeneratorService.generateNextNumber(DocumentType.RE));
        }
        recu.setMontant(recu.getMontantPaye().add(reste));
        return construireDetail(recuRepository.save(recu));
    }

    @Override
    @Transactional(readOnly = true)
    public RecuDetailResponse getRecu(Long id) {
        return construireDetail(trouverRecu(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecuListItemResponse> getRecusEnCours(String numeroRecu, String numeroClient, int page, int size) {
        // Le total d'un reçu en cours suit les nouveaux documents du client : il est recalculé à l'affichage.
        return rechercher(true, numeroRecu, numeroClient, page, size).map(r -> {
            RecuListItemResponse item = RecuListItemResponse.from(r);
            BigDecimal montant = r.getMontantPaye().add(resteClient(r.getClient().getId()));
            return new RecuListItemResponse(item.id(), item.numeroRecu(), item.clientId(), item.numeroClient(), item.nomClient(),
                    item.dateDebut(), item.dateFin(), montant, item.montantPaye(), item.valide());
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecuListItemResponse> getRecusPayes(String numeroRecu, String numeroClient, int page, int size) {
        return rechercher(false, numeroRecu, numeroClient, page, size).map(RecuListItemResponse::from);
    }

    @Override
    @Transactional
    public RecuDetailResponse valider(Long id) {
        Recu recu = trouverRecu(id);
        if (recu.isEnCours()) {
            throw new BadRequestException("Le reçu " + recu.getNumero() + " n'est pas encore entièrement payé.");
        }
        if (recu.isValide()) {
            throw new BadRequestException("Le reçu " + recu.getNumero() + " est déjà validé.");
        }
        recu.setValide(true);
        recu.setDateValidation(LocalDateTime.now());
        return construireDetail(recuRepository.save(recu));
    }

    @Override
    @Transactional
    public void supprimer(Long id) {
        Recu recu = recuRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reçu introuvable : " + id));
        if (!recu.isEnCours()) {
            throw new BadRequestException("Un reçu payé ne peut pas être supprimé.");
        }
        if (recu.getMontantPaye().signum() > 0) {
            throw new BadRequestException("Le reçu " + recu.getNumero() + " a déjà reçu des versements et ne peut pas être supprimé.");
        }
        recuRepository.delete(recu);
    }

    // ----------------------------------------------------------- versement

    @Override
    @Transactional
    public RecuDetailResponse enregistrerVersement(Long id, VersementClientRequest request) {
        Recu recu = recuRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reçu introuvable : " + id));
        if (!recu.isEnCours()) {
            throw new BadRequestException("Le reçu " + recu.getNumero() + " est déjà entièrement payé.");
        }
        BigDecimal montant = request == null ? null : request.montant();
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le montant doit être supérieur à zéro");
        }
        if (montant.stripTrailingZeros().scale() > 2) {
            throw new BadRequestException("Le montant du versement doit comporter au maximum deux décimales.");
        }

        Long clientId = recu.getClient().getId();
        String mode = normaliserMode(request.modePaiement());
        boolean parSolde = PortefeuilleClientService.MODE_PAIEMENT.equals(mode);
        String remarque = request.remarque() == null || request.remarque().isBlank() ? null : request.remarque().trim();

        List<Echeance> echeances = echeancesImpayees(clientId, !parSolde);
        BigDecimal totalDu = echeances.stream().map(Echeance::reste).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalDu.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(parSolde
                    ? "Aucune facture impayée à régler avec le solde prépayé pour ce client."
                    : "Ce client n'a aucune facture ni note de prix à régler.");
        }
        if (montant.compareTo(totalDu) > 0) {
            throw new BadRequestException("Le montant du versement dépasse le total restant à payer ("
                    + totalDu.toPlainString() + " F CFA).");
        }

        VersementClient versement = VersementClient.builder()
                .client(recu.getClient())
                .recu(recu)
                .montant(montant)
                .modePaiement(mode)
                .remarque(remarque)
                .build();

        BigDecimal resteAImputer = montant;
        for (Echeance echeance : echeances) {
            if (resteAImputer.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal part = resteAImputer.min(echeance.reste());
            if (echeance.facture() != null) {
                RecuFacture recuFacture = payerFacture(echeance.facture().getId(), part, mode, remarque);
                versement.addLigne(LigneVersementClient.builder()
                        .facture(recuFacture.getFacture()).recuFacture(recuFacture).montant(part).build());
            } else {
                NoteDePrix note = payerNoteDePrix(echeance.noteDePrix().getId(), part, mode);
                versement.addLigne(LigneVersementClient.builder()
                        .noteDePrix(note).montant(part).build());
            }
            resteAImputer = resteAImputer.subtract(part);
        }
        versementClientRepository.save(versement);
        // La collection est paresseuse : l'accès la charge depuis la base, qui contient déjà le
        // versement inséré ci-dessus. L'ajouter sans vérifier le ferait apparaître deux fois.
        List<VersementClient> versements = recu.getVersements();
        if (versements.stream().noneMatch(v -> v == versement)) {
            versements.add(0, versement);
        }

        BigDecimal resteApres = resteClient(clientId);
        recu.setMontantPaye(recu.getMontantPaye().add(montant));
        recu.setMontant(recu.getMontantPaye().add(resteApres));
        if (resteApres.signum() <= 0) {
            recu.setDateFin(LocalDateTime.now());
        }
        return construireDetail(recuRepository.save(recu));
    }

    @Override
    @Transactional
    public RecuDetailResponse annulerVersement(Long id, Long versementId) {
        Recu recu = recuRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reçu introuvable : " + id));
        VersementClient versement = recu.getVersements().stream()
                .filter(v -> v.getId().equals(versementId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Versement " + versementId + " introuvable sur le reçu " + recu.getNumero()));
        if (recu.isValide()) {
            throw new BadRequestException("Le reçu " + recu.getNumero() + " est validé : ses versements ne peuvent plus être annulés.");
        }
        Long clientId = recu.getClient().getId();
        if (!recu.isEnCours()) {
            // Un client a au plus un reçu en cours : le reçu payé ne peut repasser en cours que s'il n'y en a pas d'autre.
            recuRepository.findFirstByClientIdAndDateFinIsNullOrderByIdDesc(clientId).ifPresent(autre -> {
                throw new BadRequestException("Le client a déjà un autre reçu en cours (n° " + autre.getNumero()
                        + ") : le versement de ce reçu payé ne peut pas être annulé.");
            });
        }

        boolean parSolde = PortefeuilleClientService.MODE_PAIEMENT.equals(versement.getModePaiement());
        for (LigneVersementClient ligne : versement.getLignes()) {
            if (ligne.getFacture() != null) {
                annulerPartFacture(ligne, parSolde);
            } else if (ligne.getNoteDePrix() != null) {
                annulerPartNoteDePrix(ligne.getNoteDePrix().getId(), ligne.getMontant());
            }
        }
        recu.getVersements().remove(versement);
        versementClientRepository.delete(versement);

        recu.setMontantPaye(recu.getMontantPaye().subtract(versement.getMontant()).max(BigDecimal.ZERO));
        recu.setDateFin(null);
        recu.setMontant(recu.getMontantPaye().add(resteClient(clientId)));
        return construireDetail(recuRepository.save(recu));
    }

    private void annulerPartFacture(LigneVersementClient ligne, boolean parSolde) {
        Long factureId = ligne.getFacture().getId();
        Facture facture = factureRepository.findByIdForPaiement(factureId)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable avec l'id : " + factureId));
        BigDecimal paye = facture.getMontantPaye() == null ? BigDecimal.ZERO : facture.getMontantPaye();
        BigDecimal nouveauPaye = paye.subtract(ligne.getMontant()).max(BigDecimal.ZERO);
        facture.setMontantPaye(nouveauPaye);
        if (facture.getMontantTotal() != null) {
            facture.setResteAPayer(facture.getMontantTotal().subtract(nouveauPaye));
        }
        facture.setStatutPaiement(nouveauPaye.signum() == 0 ? StatutPaiement.NON_PAYE : StatutPaiement.PARTIEL);
        factureRepository.save(facture);

        RecuFacture recuFacture = ligne.getRecuFacture();
        if (recuFacture != null) {
            recuFacture.setAnnule(true);
            recuFacture.setDateAnnulation(LocalDateTime.now());
            recuFactureRepository.save(recuFacture);
            if (parSolde) {
                portefeuilleClientService.recrediterPaiementAnnule(facture, recuFacture, ligne.getMontant());
            }
        }
        agentNotificationService.notifyRoleInGarage(Role.AGENT, facture.getGarage(), "Paiement annulé",
                "Le reçu " + (recuFacture == null ? "" : recuFacture.getNumero() + " ") + "de la facture "
                        + facture.getNumero() + " a été annulé.");
    }

    private void annulerPartNoteDePrix(Long noteId, BigDecimal part) {
        NoteDePrix note = noteDePrixRepository.findByIdForPaiement(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note de prix introuvable avec l'id : " + noteId));
        BigDecimal total = note.getMontantTotal() == null ? BigDecimal.ZERO : note.getMontantTotal();
        BigDecimal paye = note.getMontantPaye() == null ? BigDecimal.ZERO : note.getMontantPaye();
        BigDecimal nouveauPaye = paye.subtract(part).max(BigDecimal.ZERO);
        note.setMontantPaye(nouveauPaye);
        note.setResteAPayer(total.subtract(nouveauPaye).max(BigDecimal.ZERO));
        note.setStatutPaiement(nouveauPaye.signum() == 0 ? StatutPaiement.NON_PAYE : StatutPaiement.PARTIEL);
        noteDePrixRepository.save(note);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] genererPdf(Long id, String layoutKey) {
        String layout = PdfLayout.normalize(layoutKey);
        return RecuPdf.generer(construireDetail(trouverRecu(id)), PdfLayout.AVEC_ENTETE.equals(layout));
    }

    /** Crée le reçu RC d'une facture et met à jour la facture, le solde prépayé et l'ordre de réparation. */
    private RecuFacture payerFacture(Long factureId, BigDecimal montant, String modePaiement, String remarque) {
        Facture facture = factureRepository.findByIdForPaiement(factureId)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable avec l'id : " + factureId));
        if (facture.getStatutPaiement() == StatutPaiement.PAYE) {
            throw new BadRequestException("La facture " + facture.getNumero() + " est déjà totalement payée");
        }

        BigDecimal montantPayeActuel = facture.getMontantPaye() == null ? BigDecimal.ZERO : facture.getMontantPaye();
        BigDecimal resteAPayer = facture.getResteAPayer();
        if (resteAPayer == null && facture.getMontantTotal() != null) {
            resteAPayer = facture.getMontantTotal().subtract(montantPayeActuel);
        }
        if (resteAPayer == null) {
            throw new BadRequestException("Le reste à payer de la facture " + facture.getNumero() + " est introuvable");
        }
        if (montant.compareTo(resteAPayer) > 0) {
            throw new BadRequestException("Le montant dépasse le reste à payer de la facture "
                    + facture.getNumero() + " (" + resteAPayer.toPlainString() + ")");
        }

        boolean paiementParSolde = PortefeuilleClientService.MODE_PAIEMENT.equals(modePaiement);

        // Le régime fiscal est figé sur la facture : un timbre HT est calculé à son émission.
        // Un encaissement ne doit jamais recalculer ni ajouter un second timbre, quel que soit son mode.
        RecuFacture recu = recuFactureRepository.save(RecuFacture.builder()
                .numero(documentNumberGeneratorService.generateNextNumber(DocumentType.RC))
                .facture(facture)
                .montant(montant)
                .montantTimbre(BigDecimal.ZERO)
                .remarque(remarque)
                .build());
        if (paiementParSolde) {
            portefeuilleClientService.debiterPourFacture(facture, recu, montant);
        }

        facture.setMontantPaye(montantPayeActuel.add(montant));
        facture.setResteAPayer(facture.getMontantTotal().subtract(facture.getMontantPaye()));
        boolean vehicleReadyForDelivery = false;

        if (facture.getResteAPayer().compareTo(BigDecimal.ZERO) <= 0) {
            facture.setStatutPaiement(StatutPaiement.PAYE);

            // La fiche atelier en attente de paiement peut passer à la livraison.
            OrdreReparation fiche = facture.getOrdreReparation();
            if (fiche != null && (fiche.getStatut() == StatutOrdreReparation.BL
                    || fiche.getStatut() == StatutOrdreReparation.PAIEMENT
                    || fiche.getStatut() == StatutOrdreReparation.FACTURATION
                    || fiche.getStatut() == StatutOrdreReparation.PRET_A_LIVRER)) {
                fiche.setStatut(StatutOrdreReparation.FACTURATION);
                ordreReparationRepository.save(fiche);
                vehicleReadyForDelivery = true;
                if (facture.getClient() != null) {
                    notificationService.sendInAppNotification(facture.getClient(), "Véhicule prêt à récupérer",
                            "Le paiement de votre facture est complet. Votre véhicule est prêt à être récupéré.");
                }
            }
        } else {
            facture.setStatutPaiement(StatutPaiement.PARTIEL);
        }

        factureRepository.save(facture);

        String statutLibelle = facture.getStatutPaiement() == StatutPaiement.PAYE ? "payée intégralement" : "partiellement payée";
        notificationService.sendNotification(facture.getClient(), "Paiement enregistré",
                "Le reçu " + recu.getNumero() + " de " + montant
                        + " a été enregistré. La facture " + facture.getNumero() + " est " + statutLibelle + ".");
        agentNotificationService.notifyRoleInGarage(Role.AGENT, facture.getGarage(),
                facture.getStatutPaiement() == StatutPaiement.PAYE ? "Facture payée" : "Paiement partiel reçu",
                "Le reçu " + recu.getNumero() + " a été enregistré pour la facture " + facture.getNumero() + ".");
        if (vehicleReadyForDelivery) {
            agentNotificationService.notifyRoleInGarage(Role.CHEF_ATELIER, facture.getGarage(),
                    "Véhicule prêt à livrer", "La facture " + facture.getNumero()
                            + " est soldée. Le véhicule associé peut être remis au client.");
        }
        return recu;
    }

    private NoteDePrix payerNoteDePrix(Long noteId, BigDecimal part, String mode) {
        NoteDePrix note = noteDePrixRepository.findByIdForPaiement(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note de prix introuvable avec l'id : " + noteId));
        BigDecimal total = note.getMontantTotal() == null ? BigDecimal.ZERO : note.getMontantTotal();
        BigDecimal paye = note.getMontantPaye() == null ? BigDecimal.ZERO : note.getMontantPaye();
        BigDecimal reste = DocumentRecuDto.reste(note.getResteAPayer(), total, paye);
        if (part.compareTo(reste) > 0) {
            throw new BadRequestException("Le montant dépasse le reste à payer de la note de prix "
                    + note.getNumero() + " (" + reste.toPlainString() + ").");
        }
        BigDecimal nouveauPaye = paye.add(part);
        BigDecimal nouveauReste = total.subtract(nouveauPaye).max(BigDecimal.ZERO);
        note.setMontantPaye(nouveauPaye);
        note.setResteAPayer(nouveauReste);
        note.setStatutPaiement(nouveauReste.compareTo(BigDecimal.ZERO) == 0 ? StatutPaiement.PAYE : StatutPaiement.PARTIEL);
        note.setModePaiement(mode);
        return noteDePrixRepository.save(note);
    }

    // ------------------------------------------------------------- calculs

    /** Documents impayés triés du plus ancien au plus récent (factures avant notes de prix à date égale). */
    private List<Echeance> echeancesImpayees(Long clientId, boolean inclureNotesDePrix) {
        List<Echeance> echeances = new ArrayList<>();
        for (Facture f : factureRepository.findFacturesImpayeesByClientId(clientId)) {
            echeances.add(new Echeance(f, null, f.getDateCreation(), DocumentRecuDto.from(f).resteAPayer()));
        }
        if (inclureNotesDePrix) {
            for (NoteDePrix n : noteDePrixRepository.findNotesImpayeesByClientId(clientId)) {
                echeances.add(new Echeance(null, n, n.getDateCreation(), DocumentRecuDto.from(n).resteAPayer()));
            }
        }
        echeances.removeIf(e -> e.reste().compareTo(BigDecimal.ZERO) <= 0);
        echeances.sort(Comparator.comparing(Echeance::date, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(e -> e.facture() == null ? 1 : 0));
        return echeances;
    }

    /** Reste à payer de toutes les factures et notes de prix du client. */
    private BigDecimal resteClient(Long clientId) {
        return echeancesImpayees(clientId, true).stream().map(Echeance::reste).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Documents du reçu : ceux réglés par ses versements, plus, tant qu'il est en cours,
     * les documents encore impayés du client.
     */
    private RecuDetailResponse construireDetail(Recu recu) {
        Client client = recu.getClient();
        Map<Long, Facture> factures = new LinkedHashMap<>();
        Map<Long, NoteDePrix> notes = new LinkedHashMap<>();
        for (VersementClient v : recu.getVersements()) {
            for (LigneVersementClient l : v.getLignes()) {
                if (l.getFacture() != null) {
                    factures.putIfAbsent(l.getFacture().getId(), l.getFacture());
                } else if (l.getNoteDePrix() != null) {
                    notes.putIfAbsent(l.getNoteDePrix().getId(), l.getNoteDePrix());
                }
            }
        }
        if (recu.isEnCours()) {
            factureRepository.findFacturesImpayeesByClientId(client.getId()).forEach(f -> factures.putIfAbsent(f.getId(), f));
            noteDePrixRepository.findNotesImpayeesByClientId(client.getId()).forEach(n -> notes.putIfAbsent(n.getId(), n));
        }

        Comparator<DocumentRecuDto> plusRecentDabord = Comparator.comparing(DocumentRecuDto::date,
                Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder()));
        List<DocumentRecuDto> facturesDto = factures.values().stream().map(DocumentRecuDto::from)
                .sorted(plusRecentDabord).toList();
        List<DocumentRecuDto> notesDto = notes.values().stream().map(DocumentRecuDto::from)
                .sorted(plusRecentDabord).toList();

        BigDecimal montant = recu.isEnCours()
                ? recu.getMontantPaye().add(somme(facturesDto, DocumentRecuDto::resteAPayer))
                        .add(somme(notesDto, DocumentRecuDto::resteAPayer))
                : recu.getMontant();

        return new RecuDetailResponse(
                recu.getId(),
                recu.getNumero(),
                client.getId(),
                RecuListItemResponse.numeroClient(client),
                RecuListItemResponse.from(recu).nomClient(),
                client.getPhone(),
                recu.getDateDebut(),
                recu.getDateFin(),
                recu.isEnCours(),
                recu.isValide(),
                recu.getDateValidation(),
                facturesDto,
                notesDto,
                somme(facturesDto, DocumentRecuDto::montantTotal),
                somme(notesDto, DocumentRecuDto::montantTotal),
                montant,
                recu.getMontantPaye(),
                montant.subtract(recu.getMontantPaye()).max(BigDecimal.ZERO),
                recu.getVersements().stream().map(VersementClientDto::from).toList());
    }

    private Page<Recu> rechercher(boolean enCours, String numeroRecu, String numeroClient, int page, int size) {
        Specification<Recu> spec = (root, query, cb) -> enCours
                ? cb.isNull(root.get("dateFin"))
                : cb.isNotNull(root.get("dateFin"));
        if (numeroRecu != null && !numeroRecu.isBlank()) {
            // Recherche partielle sans casse : « RE-2026-0012 » ou « 0012 » suffit.
            String motifRecu = "%" + numeroRecu.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("numero")), motifRecu));
        }
        if (numeroClient != null && !numeroClient.isBlank()) {
            // Numéro client = matricule (recherche partielle, sans casse), ou code client à défaut.
            String motif = "%" + numeroClient.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("client").get("matricule")), motif),
                    cb.like(cb.lower(root.get("client").get("codeClient")), motif)));
        }
        return recuRepository.findAll(spec,
                PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(Sort.Direction.DESC, "id")));
    }

    private Recu trouverRecu(Long id) {
        return recuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reçu introuvable : " + id));
    }

    private static String normaliserMode(String mode) {
        return mode == null || mode.isBlank() ? MODE_PAR_DEFAUT : mode.trim().toUpperCase(Locale.ROOT);
    }

    private static BigDecimal somme(List<DocumentRecuDto> docs, Function<DocumentRecuDto, BigDecimal> champ) {
        return docs.stream().map(champ).map(v -> v == null ? BigDecimal.ZERO : v).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private record Echeance(Facture facture, NoteDePrix noteDePrix, LocalDateTime date, BigDecimal reste) {
    }
}
