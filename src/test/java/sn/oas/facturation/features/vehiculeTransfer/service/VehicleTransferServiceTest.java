package sn.oas.facturation.features.vehiculeTransfer.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.service.ClientService;
import sn.oas.facturation.features.notification.service.AgentNotificationService;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.user.repository.UserRepository;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.features.vehiculeTransfer.data.dto.VehicleTransferRequestCreate;
import sn.oas.facturation.features.vehiculeTransfer.data.dto.VehicleTransferDecision;
import sn.oas.facturation.features.vehiculeTransfer.data.entity.VehicleOwnershipPeriod;
import sn.oas.facturation.features.vehiculeTransfer.data.entity.VehicleTransferRequest;
import sn.oas.facturation.features.vehiculeTransfer.data.entity.VehicleTransferStatus;
import sn.oas.facturation.features.vehiculeTransfer.repository.VehicleOwnershipPeriodRepository;
import sn.oas.facturation.features.vehiculeTransfer.repository.VehicleTransferRequestRepository;
import sn.oas.facturation.shared.exception.BadRequestException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VehicleTransferServiceTest {
    private final VehicleTransferRequestRepository requestRepository = mock(VehicleTransferRequestRepository.class);
    private final VehicleOwnershipPeriodRepository ownershipRepository = mock(VehicleOwnershipPeriodRepository.class);
    private final VehiculeRepository vehiculeRepository = mock(VehiculeRepository.class);
    private final OrdreReparationRepository ordreRepository = mock(OrdreReparationRepository.class);
    private final ClientService clientService = mock(ClientService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final AgentNotificationService notifications = mock(AgentNotificationService.class);
    private final VehicleTransferService service = new VehicleTransferService(requestRepository, ownershipRepository,
            vehiculeRepository, ordreRepository, clientService, userRepository, notifications);

    @AfterEach
    void clearSecurityContext() { SecurityContextHolder.clearContext(); }

    @Test
    void requestNormalizesPlateAndNotifiesReviewRoles() {
        Client formerOwner = client(1L, "Amadou", "Diallo");
        Client requester = client(2L, "Awa", "Ndiaye");
        Vehicule vehicle = vehicle(7L, "DK-123-AA", "CH-123", formerOwner);
        when(clientService.getClientConnecte()).thenReturn(requester);
        when(vehiculeRepository.findByImmatriculation("DK-123-AA")).thenReturn(Optional.of(vehicle));
        when(vehiculeRepository.findByIdForOwnershipChange(7L)).thenReturn(Optional.of(vehicle));
        when(requestRepository.existsByVehiculeIdAndStatus(7L, VehicleTransferStatus.PENDING)).thenReturn(false);
        when(requestRepository.save(any())).thenAnswer(invocation -> {
            var saved = invocation.<sn.oas.facturation.features.vehiculeTransfer.data.entity.VehicleTransferRequest>getArgument(0);
            saved.setId(11L);
            return saved;
        });

        var response = service.request(new VehicleTransferRequestCreate(" dk-123-aa ", "ch-123", "Véhicule vendu"));

        assertEquals(11L, response.id());
        assertEquals("DK-123-AA", response.immatriculation());
        verify(notifications).notifyRole(eq(sn.oas.facturation.features.user.data.enums.Role.AGENT),
                eq("Demande de transfert de véhicule"), contains("Awa Ndiaye"));
        verify(notifications).notifyRole(eq(sn.oas.facturation.features.user.data.enums.Role.SUPER_AGENT), anyString(), anyString());
        verify(notifications).notifyRole(eq(sn.oas.facturation.features.user.data.enums.Role.MASTER), anyString(), anyString());
    }

    @Test
    void requestRejectsMismatchedChassisBeforeCreatingRequest() {
        Client formerOwner = client(1L, "Amadou", "Diallo");
        Client requester = client(2L, "Awa", "Ndiaye");
        Vehicule vehicle = vehicle(7L, "DK-123-AA", "CH-123", formerOwner);
        when(clientService.getClientConnecte()).thenReturn(requester);
        when(vehiculeRepository.findByImmatriculation("DK-123-AA")).thenReturn(Optional.of(vehicle));
        when(vehiculeRepository.findByIdForOwnershipChange(7L)).thenReturn(Optional.of(vehicle));

        assertThrows(BadRequestException.class, () -> service.request(
                new VehicleTransferRequestCreate("DK-123-AA", "CH-999", null)));
        verify(requestRepository, never()).save(any());
        verifyNoInteractions(notifications);
    }

    @Test
    void requestRejectsPendingRequestForVehicleRegardlessOfRequester() {
        Client requester = client(2L, "Awa", "Ndiaye");
        Vehicule vehicle = vehicle(7L, "DK-123-AA", "CH-123", client(1L, "Amadou", "Diallo"));
        when(clientService.getClientConnecte()).thenReturn(requester);
        when(vehiculeRepository.findByImmatriculation("DK-123-AA")).thenReturn(Optional.of(vehicle));
        when(vehiculeRepository.findByIdForOwnershipChange(7L)).thenReturn(Optional.of(vehicle));
        when(requestRepository.existsByVehiculeIdAndStatus(7L, VehicleTransferStatus.PENDING)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.request(
                new VehicleTransferRequestCreate("DK-123-AA", null, null)));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void approvalMovesVehicleAndBackfillsUnownedLegacyOrdersToFormerOwner() {
        Client formerOwner = client(1L, "Amadou", "Diallo");
        Client requester = client(2L, "Awa", "Ndiaye");
        Vehicule vehicle = vehicle(7L, "DK-123-AA", "CH-123", formerOwner);
        VehicleTransferRequest request = VehicleTransferRequest.builder().id(11L).vehicule(vehicle)
                .requester(requester).currentOwner(formerOwner).status(VehicleTransferStatus.PENDING).build();
        OrdreReparation legacyOrder = new OrdreReparation();
        legacyOrder.setVehicule(vehicle);
        User reviewer = new User();
        reviewer.setId(90L);
        when(requestRepository.findForDecision(11L)).thenReturn(Optional.of(request));
        when(vehiculeRepository.findByIdForOwnershipChange(7L)).thenReturn(Optional.of(vehicle));
        when(ownershipRepository.findFirstByVehiculeIdAndEndedAtIsNullOrderByStartedAtDesc(7L)).thenReturn(Optional.empty());
        when(ordreRepository.findByVehiculeIdAndClientIsNull(7L)).thenReturn(java.util.List.of(legacyOrder));
        when(userRepository.findByUsername("reviewer")).thenReturn(Optional.of(reviewer));
        when(requestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("reviewer", "", java.util.List.of()));

        var response = service.decide(11L, new VehicleTransferDecision(true, "Vente vérifiée"));

        assertEquals("APPROVED", response.status());
        assertSame(requester, vehicle.getClient());
        assertSame(formerOwner, legacyOrder.getClient());
        verify(ordreRepository).flush();
        verify(ownershipRepository).save(argThat(period -> period.getClient() == formerOwner && period.getEndedAt() != null));
        verify(ownershipRepository).save(argThat(period -> period.getClient() == requester && period.getEndedAt() == null));
        verify(vehiculeRepository).save(vehicle);
    }

    private static Client client(Long id, String firstName, String lastName) {
        Client client = new Client();
        client.setId(id);
        client.setFirstName(firstName);
        client.setLastName(lastName);
        return client;
    }

    private static Vehicule vehicle(Long id, String plate, String chassis, Client owner) {
        Vehicule vehicle = new Vehicule();
        vehicle.setId(id);
        vehicle.setImmatriculation(plate);
        vehicle.setNumeroChassis(chassis);
        vehicle.setClient(owner);
        return vehicle;
    }
}
