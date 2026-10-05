package sn.oas.facturation.features.vehiculeTransfer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.service.ClientService;
import sn.oas.facturation.features.notification.service.AgentNotificationService;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.user.data.enums.Role;
import sn.oas.facturation.features.user.repository.UserRepository;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.features.vehiculeTransfer.data.dto.VehicleTransferDecision;
import sn.oas.facturation.features.vehiculeTransfer.data.dto.VehicleTransferRequestCreate;
import sn.oas.facturation.features.vehiculeTransfer.data.dto.VehicleTransferRequestResponse;
import sn.oas.facturation.features.vehiculeTransfer.data.entity.VehicleOwnershipPeriod;
import sn.oas.facturation.features.vehiculeTransfer.data.entity.VehicleTransferRequest;
import sn.oas.facturation.features.vehiculeTransfer.data.entity.VehicleTransferStatus;
import sn.oas.facturation.features.vehiculeTransfer.repository.VehicleOwnershipPeriodRepository;
import sn.oas.facturation.features.vehiculeTransfer.repository.VehicleTransferRequestRepository;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class VehicleTransferService {
    private final VehicleTransferRequestRepository requestRepository;
    private final VehicleOwnershipPeriodRepository ownershipRepository;
    private final VehiculeRepository vehiculeRepository;
    private final OrdreReparationRepository ordreRepository;
    private final ClientService clientService;
    private final UserRepository userRepository;
    private final AgentNotificationService agentNotificationService;

    @Transactional
    public VehicleTransferRequestResponse request(VehicleTransferRequestCreate payload) {
        Client requester = clientService.getClientConnecte();
        String plate = payload.immatriculation().trim().toUpperCase(Locale.ROOT);
        Vehicule found = vehiculeRepository.findByImmatriculation(plate)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun véhicule existant ne correspond à cette immatriculation."));
        Vehicule vehicle = vehiculeRepository.findByIdForOwnershipChange(found.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable."));
        if (payload.numeroChassis() != null && !payload.numeroChassis().isBlank()
                && (vehicle.getNumeroChassis() == null || !vehicle.getNumeroChassis().equalsIgnoreCase(payload.numeroChassis().trim()))) {
            throw new BadRequestException("Le numéro de châssis ne correspond pas au véhicule enregistré.");
        }
        if (requester.getId().equals(vehicle.getClient().getId())) {
            throw new BadRequestException("Ce véhicule est déjà associé à votre compte.");
        }
        if (requestRepository.existsByVehiculeIdAndRequesterIdAndStatus(vehicle.getId(), requester.getId(), VehicleTransferStatus.PENDING)) {
            throw new BadRequestException("Une demande de transfert est déjà en attente pour ce véhicule.");
        }
        VehicleTransferRequest request = requestRepository.save(VehicleTransferRequest.builder()
                .vehicule(vehicle).requester(requester).currentOwner(vehicle.getClient()).requestNote(payload.requestNote()).status(VehicleTransferStatus.PENDING).build());
        String message = "Demande de transfert du véhicule " + vehicle.getImmatriculation() + " par " + requester.getFirstName() + " " + requester.getLastName();
        agentNotificationService.notifyRole(Role.AGENT, "Demande de transfert de véhicule", message);
        agentNotificationService.notifyRole(Role.SUPER_AGENT, "Demande de transfert de véhicule", message);
        agentNotificationService.notifyRole(Role.MASTER, "Demande de transfert de véhicule", message);
        return VehicleTransferRequestResponse.forClient(request);
    }

    @Transactional(readOnly = true)
    public List<VehicleTransferRequestResponse> myRequests() {
        Client requester = clientService.getClientConnecte();
        return requestRepository.findByRequesterIdOrderByRequestedAtDesc(requester.getId()).stream()
                .map(VehicleTransferRequestResponse::forClient).toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleTransferRequestResponse> pendingRequests() {
        return requestRepository.findByStatusOrderByRequestedAtAsc(VehicleTransferStatus.PENDING).stream()
                .map(VehicleTransferRequestResponse::forAgent).toList();
    }

    @Transactional
    public VehicleTransferRequestResponse decide(Long id, VehicleTransferDecision decision) {
        if (decision == null) throw new BadRequestException("La décision de transfert est obligatoire.");
        VehicleTransferRequest request = requestRepository.findForDecision(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de transfert introuvable."));
        if (request.getStatus() != VehicleTransferStatus.PENDING) throw new BadRequestException("Cette demande a déjà été traitée.");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User reviewer = userRepository.findByUsername(username).orElseGet(() -> userRepository.findByEmail(username)
                .orElseThrow(() -> new AccessDeniedException("Utilisateur de décision introuvable.")));
        LocalDateTime now = LocalDateTime.now();
        request.setReviewedBy(reviewer);
        request.setDecidedAt(now);
        request.setDecisionNote(decision.decisionNote());
        if (!decision.approved()) {
            request.setStatus(VehicleTransferStatus.REJECTED);
            return VehicleTransferRequestResponse.forAgent(requestRepository.save(request));
        }

        Vehicule vehicle = vehiculeRepository.findByIdForOwnershipChange(request.getVehicule().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable."));
        Client formerOwner = vehicle.getClient();
        if (!formerOwner.getId().equals(request.getCurrentOwner().getId())) {
            request.setStatus(VehicleTransferStatus.REJECTED);
            request.setDecisionNote("Le propriétaire du véhicule a changé pendant l'examen de la demande.");
            return VehicleTransferRequestResponse.forAgent(requestRepository.save(request));
        }
        VehicleOwnershipPeriod current = ownershipRepository.findFirstByVehiculeIdAndEndedAtIsNullOrderByStartedAtDesc(vehicle.getId()).orElse(null);
        if (current == null) {
            ownershipRepository.save(VehicleOwnershipPeriod.builder().vehicule(vehicle).client(formerOwner)
                    .startedAt(vehicle.getCreatedAt() != null ? vehicle.getCreatedAt() : now).endedAt(now).build());
        } else {
            current.setEndedAt(now);
            ownershipRepository.save(current);
        }
        ordreRepository.findByVehiculeIdAndClientIsNull(vehicle.getId()).forEach(order -> order.setClient(formerOwner));
        ordreRepository.flush();
        ownershipRepository.save(VehicleOwnershipPeriod.builder().vehicule(vehicle).client(request.getRequester())
                .startedAt(now).transferRequest(request).build());
        vehicle.setClient(request.getRequester());
        vehicle.setArchiveParClient(false);
        vehiculeRepository.save(vehicle);
        request.setStatus(VehicleTransferStatus.APPROVED);
        return VehicleTransferRequestResponse.forAgent(requestRepository.save(request));
    }
}
