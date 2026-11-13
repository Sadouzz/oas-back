package sn.oas.facturation.features.rendezvous.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.client.service.PolitiqueFinanciereClientService;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.garage.repository.GarageRepository;
import sn.oas.facturation.features.notification.service.NotificationService;
import sn.oas.facturation.features.notification.service.AgentNotificationService;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.ordreReparation.service.OrdreReparationService;
import sn.oas.facturation.features.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.features.rendezvous.data.entity.DemandeDerogationPlafond;
import sn.oas.facturation.features.rendezvous.data.enums.RendezVousStatus;
import sn.oas.facturation.features.rendezvous.data.enums.StatutDemandeDerogationPlafond;
import sn.oas.facturation.features.rendezvous.dto.DemandeDerogationPlafondResponse;
import sn.oas.facturation.features.rendezvous.dto.RendezVousCreationResult;
import sn.oas.facturation.features.rendezvous.dto.RendezVousRequest;
import sn.oas.facturation.features.rendezvous.dto.RendezVousResponse;
import sn.oas.facturation.features.clientportal.dto.ReservationBookingStatusDTO;
import sn.oas.facturation.features.client.service.PolitiqueFinanciereClientService.EvaluationReservation;
import sn.oas.facturation.features.rendezvous.repository.DemandeDerogationPlafondRepository;
import sn.oas.facturation.features.rendezvous.repository.RendezVousRepository;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.documentNumber.DocumentType;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ForbiddenException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RendezVousServiceImpl implements RendezVousService {

    private final RendezVousRepository rendezvousRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ClientRepository clientRepository;
    private final GarageRepository garageRepository;
    private final NotificationService notificationService;
    private final AgentNotificationService agentNotificationService;
    private final OrdreReparationService ordreReparationService;
    private final DocumentNumberGeneratorService documentNumberGeneratorService;
    private final PolitiqueFinanciereClientService politiqueFinanciereClientService;
    private final DemandeDerogationPlafondRepository demandeDerogationRepository;

    @Transactional
    @Override
    public RendezVousCreationResult createRendezVousByAdmin(RendezVousRequest request, String requestedBy, String requesterRole) {
        if (request.dateRendezVous() == null) {
            throw new BadRequestException("La date du rendez-vous est obligatoire");
        }
        if (request.dateRendezVous().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La date du rendez-vous ne peut pas être dans le passé");
        }

        Vehicule vehicule = null;
        if (request.vehiculeId() != null) {
            vehicule = vehiculeRepository.findById(request.vehiculeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'identifiant " + request.vehiculeId()));
        }

        Long resolvedClientId = request.clientId();
        if (resolvedClientId == null && vehicule != null && vehicule.getClient() != null) {
            resolvedClientId = vehicule.getClient().getId();
        } else {
            if (resolvedClientId == null) {
                throw new BadRequestException("Veuillez spécifier un client ou sélectionner un véhicule associé à un client");
            }
        }

        Long clientIdForLock = resolvedClientId;
        Client client = clientRepository.findByIdForUpdate(clientIdForLock)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'identifiant " + clientIdForLock));

        if (vehicule != null && vehicule.getClient() != null && !vehicule.getClient().getId().equals(client.getId())) {
            throw new BadRequestException("Le véhicule sélectionné n'appartient pas au client sélectionné");
        }
        validateNoExistingWorkflow(vehicule);

        Garage garage = null;
        if (request.garageId() != null) {
            garage = garageRepository.findById(request.garageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Garage non trouvé avec l'identifiant " + request.garageId()));
        } else {
            garage = documentNumberGeneratorService.getCurrentGarage();
            if (garage == null) {
                garage = garageRepository.findAll().stream().findFirst().orElse(null);
            }
        }

        RendezVousStatus statut = request.statut() != null ? request.statut() : RendezVousStatus.CONFIRME;
        if (statut == RendezVousStatus.REFUSE || statut == RendezVousStatus.ANNULE || statut == RendezVousStatus.TERMINE) {
            throw new BadRequestException("Un rendez-vous ne peut pas être créé directement avec un statut terminé ou annulé.");
        }

        return createOrRequest(client, vehicule, garage, request, statut,
                requesterRole == null || requesterRole.isBlank() ? "AGENT" : requesterRole, requestedBy);
    }

    @Transactional
    @Override
    public RendezVousCreationResult bookRendezVous(Client connectedClient, RendezVousRequest request, String requestedBy) {
        Client client = clientRepository.findByIdForUpdate(connectedClient.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));
        if (request.vehiculeId() == null) {
            throw new BadRequestException("Veuillez sélectionner un véhicule");
        }
        if (request.dateRendezVous() == null) {
            throw new BadRequestException("La date du rendez-vous est obligatoire");
        }
        if (request.dateRendezVous().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La date du rendez-vous ne peut pas être dans le passé");
        }

        Vehicule vehicule = vehiculeRepository.findById(request.vehiculeId())
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'identifiant " + request.vehiculeId()));
        if (!vehicule.getClient().getId().equals(client.getId())) {
            throw new ForbiddenException("Le véhicule n'appartient pas au client connecté");
        }
        validateNoExistingWorkflow(vehicule);

        Garage garage = null;
        if (request.garageId() != null) {
            garage = garageRepository.findById(request.garageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Garage non trouvé"));
        } else {
            throw new BadRequestException("Veuillez sélectionner un garage");
        }

        return createOrRequest(client, vehicule, garage, request, RendezVousStatus.EN_ATTENTE, "CLIENT", requestedBy);
    }

    private void validateNoExistingWorkflow(Vehicule vehicule) {
        if (vehicule == null) return;
        if (rendezvousRepository.existsByVehiculeIdAndStatut(vehicule.getId(), RendezVousStatus.EN_ATTENTE)) {
            throw new BadRequestException("Ce véhicule a déjà un rendez-vous en attente");
        }
        if (rendezvousRepository.existsByVehiculeIdAndStatutAndDateRendezVousAfter(
                vehicule.getId(), RendezVousStatus.CONFIRME, LocalDateTime.now())) {
            throw new BadRequestException("Ce véhicule a déjà un rendez-vous confirmé à venir");
        }
        if (ordreReparationService.existsByVehiculeIdAndStatutNotIn(vehicule.getId(),
                List.of(StatutOrdreReparation.PRET_A_LIVRER, StatutOrdreReparation.LIVRE))) {
            throw new BadRequestException("Ce véhicule est actuellement en cours d'intervention/réparation");
        }
    }

    private RendezVousCreationResult createOrRequest(Client client, Vehicule vehicule, Garage garage,
            RendezVousRequest request, RendezVousStatus statut, String origine, String requestedBy) {
        EvaluationReservation evaluation = politiqueFinanciereClientService.evaluerReservation(client);
        if (evaluation.factureEchueImpayee()) {
            throw new BadRequestException(evaluation.motifBlocage());
        }

        Optional<DemandeDerogationPlafond> demandeEnAttente = demandeDerogationRepository
                .findFirstByClientIdAndStatutOrderByDateDemandeDesc(
                        client.getId(), StatutDemandeDerogationPlafond.EN_ATTENTE);
        if (evaluation.plafondAtteint() || demandeEnAttente.isPresent()) {
            if (demandeEnAttente.isPresent()) {
                return RendezVousCreationResult.dejaEnAttente(
                        DemandeDerogationPlafondResponse.from(demandeEnAttente.get()));
            }
            if (vehicule == null) {
                throw new BadRequestException("Sélectionnez le véhicule concerné pour soumettre une demande de dérogation.");
            }
            DemandeDerogationPlafond demande = DemandeDerogationPlafond.builder()
                    .client(client)
                    .vehicule(vehicule)
                    .garage(garage)
                    .dateRendezVous(request.dateRendezVous())
                    .motif(request.motif())
                    .commentaire(request.commentaire())
                    .statutRendezVousDemande(statut)
                    .statut(StatutDemandeDerogationPlafond.EN_ATTENTE)
                    .origine(origine)
                    .demandeur(requestedBy == null || requestedBy.isBlank() ? "inconnu" : requestedBy)
                    .plafondSnapshot(evaluation.plafond())
                    .montantConsommeSnapshot(evaluation.montantConsomme())
                    .echeanceJoursSnapshot(evaluation.echeanceJours())
                    .build();
            demande = demandeDerogationRepository.save(demande);
            String clientName = (client.getFirstName() + " " + client.getLastName()).trim();
            String message = "Le plafond de " + clientName + " est atteint ("
                    + evaluation.montantConsomme() + " / " + evaluation.plafond()
                    + " F CFA). Une demande de nouveau rendez-vous pour le véhicule "
                    + vehicule.getImmatriculation() + " attend une décision. Demande n°" + demande.getId() + ".";
            agentNotificationService.notifyRole(sn.oas.facturation.features.user.data.enums.Role.SUPER_AGENT,
                    "Dérogation de plafond à examiner", message);
            return RendezVousCreationResult.enAttente(DemandeDerogationPlafondResponse.from(demande));
        }

        RendezVous rv = persistAppointment(client, vehicule, garage, request.dateRendezVous(), request.motif(),
                statut, request.commentaire());
        notifyAppointmentCreated(rv, false);
        return RendezVousCreationResult.cree(RendezVousResponse.of(rv));
    }

    private RendezVous persistAppointment(Client client, Vehicule vehicule, Garage garage,
            LocalDateTime dateRendezVous, String motif, RendezVousStatus statut, String commentaire) {
        RendezVous rv = RendezVous.builder()
                .numero(documentNumberGeneratorService.generateNextNumber(garage, DocumentType.RDV))
                .client(client)
                .vehicule(vehicule)
                .garage(garage)
                .dateRendezVous(dateRendezVous)
                .motif(motif)
                .statut(statut)
                .commentaire(commentaire)
                .build();
        return rendezvousRepository.save(rv);
    }

    private void notifyAppointmentCreated(RendezVous rv, boolean derogationApproved) {
        if (rv.getGarage() != null) {
            String title = rv.getStatut() == RendezVousStatus.EN_ATTENTE
                    ? "Nouvelle demande de rendez-vous" : "Nouveau rendez-vous";
            String message = rv.getStatut() == RendezVousStatus.EN_ATTENTE
                    ? "Une demande de rendez-vous pour le " + rv.getDateRendezVous() + " est en attente de confirmation."
                    : "Un rendez-vous a été enregistré pour le " + rv.getDateRendezVous() + ".";
            agentNotificationService.notifyRoleInGarage(
                    sn.oas.facturation.features.user.data.enums.Role.CHEF_ATELIER, rv.getGarage(), title, message);
        }
        if (derogationApproved) {
            notificationService.sendNotification(rv.getClient(), "Dérogation de plafond approuvée",
                    "Votre demande exceptionnelle a été approuvée. "
                            + (rv.getStatut() == RendezVousStatus.EN_ATTENTE ? "Votre demande de rendez-vous" : "Votre rendez-vous")
                            + " pour le " + rv.getDateRendezVous() + " est maintenant enregistrée.");
            return;
        }
        String message = rv.getStatut() == RendezVousStatus.EN_ATTENTE
                ? "Votre demande de rendez-vous pour le " + rv.getDateRendezVous()
                    + " a bien été enregistrée et est en attente de confirmation."
                : "Un rendez-vous a été programmé pour vous le " + rv.getDateRendezVous()
                    + (rv.getMotif() != null && !rv.getMotif().isBlank() ? " (" + rv.getMotif() + ")" : "") + ".";
        String title = rv.getStatut() == RendezVousStatus.EN_ATTENTE
                ? "Demande de rendez-vous enregistrée" : "Rendez-vous programmé";
        notificationService.sendNotification(rv.getClient(), title, message);
    }

    @Transactional(readOnly = true)
    @Override
    public ReservationBookingStatusDTO getClientBookingStatus(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));
        EvaluationReservation evaluation = politiqueFinanciereClientService.evaluerReservation(client);
        Optional<DemandeDerogationPlafond> pending = demandeDerogationRepository
                .findFirstByClientIdAndStatutOrderByDateDemandeDesc(clientId, StatutDemandeDerogationPlafond.EN_ATTENTE);
        String message = evaluation.motifBlocage();
        if (pending.isPresent() && !evaluation.factureEchueImpayee()) {
            message = "Votre demande exceptionnelle est en attente d'examen par un Super Agent.";
        }
        return new ReservationBookingStatusDTO(
                evaluation.workflowAutorise() && pending.isEmpty(),
                evaluation.plafondAtteint(),
                evaluation.factureEchueImpayee(),
                evaluation.derogationPossible() && pending.isEmpty(),
                pending.isPresent(),
                message,
                pending.map(DemandeDerogationPlafond::getDateDemande).orElse(null));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<DemandeDerogationPlafondResponse> getPendingQuotaDerogations(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));
        return demandeDerogationRepository.findAllByStatutOrderByDateDemandeAsc(
                        StatutDemandeDerogationPlafond.EN_ATTENTE, PageRequest.of(safePage, safeSize))
                .map(DemandeDerogationPlafondResponse::from);
    }

    @Transactional
    @Override
    public RendezVous approveQuotaDerogation(Long requestId, String decisionMaker, String reason) {
        validateDecisionReason(reason);
        DemandeDerogationPlafond demande = demandeDerogationRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de dérogation introuvable"));
        requirePendingDecision(demande);
        // Serialize this approval with all other workflow entry points for the same client.
        Client client = clientRepository.findByIdForUpdate(demande.getClient().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));
        String overdue = politiqueFinanciereClientService.motifBlocageRetard(client.getId());
        if (overdue != null) throw new BadRequestException(overdue);
        if (demande.getDateRendezVous().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La date demandée est passée. Refusez cette demande et soumettez-en une nouvelle avec une date à venir.");
        }
        validateNoExistingWorkflow(demande.getVehicule());

        RendezVous rv = persistAppointment(client, demande.getVehicule(), demande.getGarage(),
                demande.getDateRendezVous(), demande.getMotif(), demande.getStatutRendezVousDemande(),
                demande.getCommentaire());
        demande.setStatut(StatutDemandeDerogationPlafond.APPROUVEE);
        demande.setDecisionnaire(decisionMaker);
        demande.setDateDecision(LocalDateTime.now());
        demande.setMotifDecision(reason.trim());
        demande.setRendezVous(rv);
        demandeDerogationRepository.save(demande);
        notifyAppointmentCreated(rv, true);
        return rv;
    }

    @Transactional
    @Override
    public DemandeDerogationPlafondResponse rejectQuotaDerogation(Long requestId, String decisionMaker, String reason) {
        validateDecisionReason(reason);
        DemandeDerogationPlafond demande = demandeDerogationRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de dérogation introuvable"));
        requirePendingDecision(demande);
        demande.setStatut(StatutDemandeDerogationPlafond.REFUSEE);
        demande.setDecisionnaire(decisionMaker);
        demande.setDateDecision(LocalDateTime.now());
        demande.setMotifDecision(reason.trim());
        demandeDerogationRepository.save(demande);
        notificationService.sendNotification(demande.getClient(), "Demande exceptionnelle refusée",
                "Votre demande de rendez-vous du " + demande.getDateRendezVous()
                        + " n'a pas été approuvée. Motif : " + reason.trim());
        if (!"CLIENT".equals(demande.getOrigine()) && demande.getGarage() != null) {
            try {
                var role = sn.oas.facturation.features.user.data.enums.Role.valueOf(demande.getOrigine());
                agentNotificationService.notifyRoleInGarage(role, demande.getGarage(), "Demande de dérogation refusée",
                        "La demande n°" + demande.getId() + " pour " + demande.getClient().getFirstName() + " "
                                + demande.getClient().getLastName() + " a été refusée par un Super Agent.");
            } catch (IllegalArgumentException ignored) {
                // A legacy or unknown requester role must not prevent a valid decision.
            }
        }
        return DemandeDerogationPlafondResponse.from(demande);
    }

    private void requirePendingDecision(DemandeDerogationPlafond demande) {
        if (demande.getStatut() != StatutDemandeDerogationPlafond.EN_ATTENTE) {
            throw new BadRequestException("Cette demande a déjà fait l'objet d'une décision.");
        }
    }

    private void validateDecisionReason(String reason) {
        if (reason == null || reason.trim().length() < 3) {
            throw new BadRequestException("Un motif d'au moins 3 caractères est obligatoire pour la décision.");
        }
        if (reason.length() > 1000) {
            throw new BadRequestException("Le motif de décision ne peut pas dépasser 1 000 caractères.");
        }
    }

    @Transactional
    @Override
    public RendezVous cancelRendezVous(Client client, Long id) {
        return cancelRendezVous(client, id, null);
    }

    @Transactional
    @Override
    public RendezVous cancelRendezVous(Client client, Long id, String motifAnnulation) {
        if (motifAnnulation == null || motifAnnulation.isBlank()) {
            throw new BadRequestException("Veuillez indiquer le motif de l'annulation.");
        }
        RendezVous rv = rendezvousRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'identifiant " + id));
        if (!rv.getClient().getId().equals(client.getId())) {
            throw new ForbiddenException("Accès non autorisé à ce rendez-vous");
        }
        requireActiveVehicleForAppointmentAction(rv);
        if (rv.getStatut() != RendezVousStatus.EN_ATTENTE) {
            throw new BadRequestException("Seul un rendez-vous en attente peut être annulé par le client.");
        }
        if (rv.getFicheAtelier() != null) {
            throw new BadRequestException("Impossible d'annuler le rendez-vous : une fiche d'atelier a déjà été créée.");
        }
        rv.setStatut(RendezVousStatus.ANNULE);
        rv.setMotifAnnulation(normalizeMotif(motifAnnulation));
        rendezvousRepository.save(rv);

        String message = cancellationMessage(rv);
        notificationService.sendNotification(client, "Rendez-vous annulé", message);

        return rv;
    }

    @Transactional(readOnly = true)
    @Override
    public List<RendezVous> getClientRendezVous(Client client) {
        return getRendezVousByClientId(client.getId());
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RendezVous> getClientRendezVous(Client client, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        return rendezvousRepository.findByClientId(client.getId(), pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RendezVous> getRendezVousByClientId(Long clientId) {
        return rendezvousRepository.findByClientIdOrderByDateCreationDesc(clientId);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RendezVous> getRendezVousByClientId(Long clientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        return rendezvousRepository.findByClientId(clientId, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RendezVous> getClientRendezVousByStatus(Client client, RendezVousStatus status) {
        return rendezvousRepository.findByClientIdAndStatutOrderByDateCreationDesc(client.getId(), status);
    }

    @Transactional(readOnly = true)
    @Override
    public RendezVous getById(Long id) {
        return rendezvousRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'identifiant " + id));
    }

    @Transactional(readOnly = true)
    @Override
    public List<RendezVous> getAllRendezVous() {
        return rendezvousRepository.findAllByOrderByDateCreationDesc();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RendezVous> getAllRendezVous(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        return rendezvousRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RendezVous> searchRendezVous(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        return rendezvousRepository.searchRendezVous(keyword, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RendezVous> getByStatut(RendezVousStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        return rendezvousRepository.findByStatut(status, pageable);
    }

    @Transactional
    @Override
    public RendezVous updateRendezVousStatus(Long id, RendezVousStatus status, String commentaire) {
        return updateRendezVousStatus(id, status, commentaire, null);
    }

    @Transactional
    @Override
    public RendezVous updateRendezVousStatus(Long id, RendezVousStatus status, String commentaire, String motifAnnulation) {
        if (status == RendezVousStatus.REFUSE) {
            status = RendezVousStatus.ANNULE;
        }
        RendezVous rv = rendezvousRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'identifiant " + id));
        requireActiveVehicleForAppointmentAction(rv);

        if (status == RendezVousStatus.ANNULE && (motifAnnulation == null || motifAnnulation.isBlank())) {
            throw new BadRequestException("Le motif est obligatoire pour annuler le rendez-vous.");
        }
        
        if (status == RendezVousStatus.ANNULE && rv.getFicheAtelier() != null) {
            throw new BadRequestException("Impossible d'annuler le rendez-vous : une fiche d'atelier a déjà été créée.");
        }
        
        rv.setStatut(status);
        if (status == RendezVousStatus.ANNULE) {
            rv.setMotifAnnulation(normalizeMotif(motifAnnulation));
        } else {
            rv.setMotifAnnulation(null);
        }
        if (commentaire != null) {
            rv.setCommentaire(commentaire);
        }
        rendezvousRepository.save(rv);

        // Notify client
        String titre = status == RendezVousStatus.ANNULE ? "Rendez-vous annulé" : "Mise à jour du rendez-vous";
        String message = status == RendezVousStatus.ANNULE
                ? cancellationMessage(rv)
                : "Votre rendez-vous du " + rv.getDateRendezVous() + " est maintenant " + statusLabel(status) + ".";
        if (commentaire != null && !commentaire.trim().isEmpty()) {
            message += " Commentaire : " + commentaire;
        }
        notificationService.sendNotification(rv.getClient(), titre, message);

        return rv;
    }

    private String normalizeMotif(String motif) {
        if (motif == null || motif.isBlank()) return null;
        String normalized = motif.trim();
        if (normalized.length() > 1000) {
            throw new BadRequestException("Le motif d'annulation ne peut pas dépasser 1 000 caractères.");
        }
        return normalized;
    }

    private void requireActiveVehicleForAppointmentAction(RendezVous rendezVous) {
        if (rendezVous != null && rendezVous.getVehicule() != null) {
            sn.oas.facturation.features.vehicule.service.VehiculeActivationPolicy.requireActive(rendezVous.getVehicule());
        }
    }

    private String cancellationMessage(RendezVous rv) {
        String date = rv.getDateRendezVous() == null ? "date non précisée"
                : rv.getDateRendezVous().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm"));
        String message = "Votre rendez-vous prévu à la date du " + date
                + " a été annulé";
        if (rv.getMotifAnnulation() != null && !rv.getMotifAnnulation().isBlank()) {
            message += " pour motif de « " + rv.getMotifAnnulation() + " »";
        }
        return message + ".";
    }

    private String statusLabel(RendezVousStatus status) {
        return switch (status) {
            case CONFIRME -> "confirmé";
            case EN_ATTENTE -> "en attente";
            case TERMINE -> "terminé";
            case REFUSE -> "annulé";
            case ANNULE -> "annulé";
        };
    }

    @Transactional
    @Override
    public RendezVous validerRendezVous(Long id, List<Long> mecanicienIds) {
        RendezVous rv = rendezvousRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'identifiant " + id));
        requireActiveVehicleForAppointmentAction(rv);
        if (rv.getVehicule() == null) {
            throw new BadRequestException("Impossible de valider un rendez-vous sans véhicule associé.");
        }
        
        rv.setStatut(RendezVousStatus.CONFIRME);
        rendezvousRepository.save(rv);

        String message = "Votre rendez-vous prévu à la date du " + rv.getDateRendezVous() + " a été validé et confirmé.";
        notificationService.sendNotification(rv.getClient(), "Rendez-vous validé", message);

        return rv;
    }

    @Transactional
    @Override
    public RendezVous updateDate(Long id, LocalDateTime nouvelleDate) {
        RendezVous rv = rendezvousRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'identifiant " + id));
        requireActiveVehicleForAppointmentAction(rv);
        if (nouvelleDate == null) {
            throw new BadRequestException("La date du rendez-vous est obligatoire");
        }
        if (nouvelleDate.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La nouvelle date ne peut pas être dans le passé");
        }
        rv.setDateRendezVous(nouvelleDate);
        rendezvousRepository.save(rv);

        String message = "La date de votre rendez-vous a été modifiée au " + nouvelleDate + ".";
        if (rv.getClient() != null) {
            notificationService.sendNotification(rv.getClient(), "Date de rendez-vous modifiée",
                    message);
        }

        return rv;
    }

    @Transactional
    @Override
    public RendezVous updateRendezVous(Long id, RendezVousRequest request) {
        RendezVous rv = rendezvousRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'identifiant " + id));
        requireActiveVehicleForAppointmentAction(rv);
        RendezVousStatus ancienStatut = rv.getStatut();

        if (request.dateRendezVous() != null) {
            if (request.dateRendezVous().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("La date du rendez-vous ne peut pas être dans le passé");
            }
            rv.setDateRendezVous(request.dateRendezVous());
        }

        if (request.motif() != null) {
            rv.setMotif(request.motif());
        }

        if (request.commentaire() != null) {
            rv.setCommentaire(request.commentaire());
        }

        RendezVousStatus nouveauStatut = request.statut() == RendezVousStatus.REFUSE
                ? RendezVousStatus.ANNULE : request.statut();
        if (nouveauStatut != null) {
            if (nouveauStatut == RendezVousStatus.ANNULE
                    && (request.motifAnnulation() == null || request.motifAnnulation().isBlank())) {
                throw new BadRequestException("Le motif est obligatoire pour annuler le rendez-vous.");
            }
            if (nouveauStatut == RendezVousStatus.ANNULE && rv.getFicheAtelier() != null) {
                throw new BadRequestException("Impossible d'annuler le rendez-vous : une fiche d'atelier a déjà été créée.");
            }
            rv.setStatut(nouveauStatut);
            if (nouveauStatut == RendezVousStatus.ANNULE) {
                rv.setMotifAnnulation(normalizeMotif(request.motifAnnulation()));
            } else {
                rv.setMotifAnnulation(null);
            }
        }

        if (request.vehiculeId() != null && (rv.getVehicule() == null || !request.vehiculeId().equals(rv.getVehicule().getId()))) {
            Vehicule vehicule = vehiculeRepository.findById(request.vehiculeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'identifiant " + request.vehiculeId()));
            rv.setVehicule(vehicule);
        }

        if (request.clientId() != null && (rv.getClient() == null || !request.clientId().equals(rv.getClient().getId()))) {
            Client client = clientRepository.findById(request.clientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'identifiant " + request.clientId()));
            rv.setClient(client);
        }

        if (request.garageId() != null && (rv.getGarage() == null || !request.garageId().equals(rv.getGarage().getId()))) {
            Garage garage = garageRepository.findById(request.garageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Garage non trouvé avec l'identifiant " + request.garageId()));
            rv.setGarage(garage);
        }

        rendezvousRepository.save(rv);

        if (nouveauStatut != null && nouveauStatut != ancienStatut) {
            String titre = nouveauStatut == RendezVousStatus.ANNULE ? "Rendez-vous annulé" : "Mise à jour du rendez-vous";
            String message = nouveauStatut == RendezVousStatus.ANNULE
                    ? cancellationMessage(rv)
                    : "Votre rendez-vous du " + rv.getDateRendezVous() + " est maintenant " + statusLabel(nouveauStatut) + ".";
            notificationService.sendNotification(rv.getClient(), titre, message);
        } else if (rv.getClient() != null && request.dateRendezVous() != null) {
            notificationService.sendNotification(rv.getClient(), "Rendez-vous modifié",
                    "Les informations de votre rendez-vous ont été mises à jour (Date : " + rv.getDateRendezVous() + ").");
        }

        return rv;
    }
}
