package sn.oas.facturation.features.rendezvous.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.client.service.PolitiqueFinanciereClientService;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.garage.repository.GarageRepository;
import sn.oas.facturation.features.notification.service.AgentNotificationService;
import sn.oas.facturation.features.notification.service.NotificationService;
import sn.oas.facturation.features.ordreReparation.service.OrdreReparationService;
import sn.oas.facturation.features.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.features.rendezvous.data.entity.DemandeDerogationPlafond;
import sn.oas.facturation.features.rendezvous.data.enums.RendezVousStatus;
import sn.oas.facturation.features.rendezvous.data.enums.StatutDemandeDerogationPlafond;
import sn.oas.facturation.features.rendezvous.dto.RendezVousRequest;
import sn.oas.facturation.features.client.service.PolitiqueFinanciereClientService.EvaluationReservation;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.rendezvous.repository.RendezVousRepository;
import sn.oas.facturation.features.rendezvous.repository.DemandeDerogationPlafondRepository;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.exception.BadRequestException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RendezVousServiceImplTest {

    @Mock RendezVousRepository rendezvousRepository;
    @Mock VehiculeRepository vehiculeRepository;
    @Mock ClientRepository clientRepository;
    @Mock GarageRepository garageRepository;
    @Mock NotificationService notificationService;
    @Mock AgentNotificationService agentNotificationService;
    @Mock OrdreReparationService ordreReparationService;
    @Mock DocumentNumberGeneratorService documentNumberGeneratorService;
    @Mock PolitiqueFinanciereClientService politiqueFinanciereClientService;
    @Mock DemandeDerogationPlafondRepository demandeDerogationRepository;

    @InjectMocks RendezVousServiceImpl service;

    private Client client;
    private RendezVous rendezVous;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(4L)
                .firstName("Awa")
                .lastName("Diop")
                .email("awa@example.com")
                .phone("+221770000000")
                .build();
        rendezVous = RendezVous.builder()
                .id(12L)
                .client(client)
                .dateRendezVous(LocalDateTime.of(2026, 10, 12, 9, 30))
                .motif("Vidange")
                .statut(RendezVousStatus.EN_ATTENTE)
                .build();
        lenient().when(rendezvousRepository.findById(12L)).thenReturn(Optional.of(rendezVous));
    }

    @Test
    void refusesCancellationWithoutReason() {
        assertThrows(BadRequestException.class,
                () -> service.updateRendezVousStatus(12L, RendezVousStatus.ANNULE, null, " "));

        verify(rendezvousRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void cancellationPersistsReasonAndDelegatesNotificationDelivery() {
        when(rendezvousRepository.save(any(RendezVous.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.updateRendezVousStatus(12L, RendezVousStatus.ANNULE, null, "Client indisponible");

        assertEquals("Client indisponible", rendezVous.getMotifAnnulation());
        verify(notificationService).sendNotification(eq(client), eq("Rendez-vous annulé"),
                contains("Votre rendez-vous prévu à la date du 12/10/2026 à 09:30 a été annulé pour motif de « Client indisponible »"));
    }

    @Test
    void clientMayBookAppointmentForVehicleAwaitingActivation() {
        Vehicule vehicule = Vehicule.builder().client(client).immatriculation("DK-7788-AA")
                .marque("Opel").modele("Corsa").kilometrage(1000.0).actif(false).build();
        vehicule.setId(77L);
        Garage garage = Garage.builder().nom("Dakar").localite("Dakar").prefixe("DK").build();
        garage.setId(3L);
        when(clientRepository.findByIdForUpdate(client.getId())).thenReturn(Optional.of(client));
        when(politiqueFinanciereClientService.evaluerReservation(client)).thenReturn(
                new EvaluationReservation(true, false, false, null, null, java.math.BigDecimal.ZERO, null));
        when(vehiculeRepository.findById(77L)).thenReturn(Optional.of(vehicule));
        when(garageRepository.findById(3L)).thenReturn(Optional.of(garage));
        when(documentNumberGeneratorService.generateNextNumber(garage, sn.oas.facturation.shared.documentNumber.DocumentType.RDV))
                .thenReturn("RDV-DK-001");
        when(rendezvousRepository.save(any(RendezVous.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(demandeDerogationRepository.findFirstByClientIdAndStatutOrderByDateDemandeDesc(
                client.getId(), StatutDemandeDerogationPlafond.EN_ATTENTE)).thenReturn(Optional.empty());

        var result = service.bookRendezVous(client,
                new RendezVousRequest(LocalDateTime.now().plusDays(1), "Diagnostic", 77L, 3L), "awa@example.com");

        assertEquals("RENDEZ_VOUS_CREE", result.type().name());
        assertEquals(77L, result.rendezVous().vehiculeId());
        assertEquals(RendezVousStatus.EN_ATTENTE, result.rendezVous().statut());
        verify(agentNotificationService).notifyRoleInGarage(
                sn.oas.facturation.features.user.data.enums.Role.CHEF_ATELIER,
                garage,
                "Nouvelle demande de rendez-vous",
                "Une demande de rendez-vous pour le " + result.rendezVous().dateRendezVous() + " est en attente de confirmation.");
    }

    @Test
    void demandeUneDerogationAuLieuDeCreerLeRendezVousQuandLePlafondEstAtteint() {
        Vehicule vehicule = Vehicule.builder().client(client).immatriculation("DK-7788-AA")
                .marque("Opel").modele("Corsa").kilometrage(1000.0).actif(true).build();
        vehicule.setId(77L);
        Garage garage = Garage.builder().nom("Dakar").localite("Dakar").prefixe("DK").build();
        garage.setId(3L);
        when(clientRepository.findByIdForUpdate(client.getId())).thenReturn(Optional.of(client));
        when(politiqueFinanciereClientService.evaluerReservation(client)).thenReturn(
                new EvaluationReservation(false, true, false, "Plafond atteint", new java.math.BigDecimal("500000"),
                        new java.math.BigDecimal("500000"), 30));
        when(vehiculeRepository.findById(77L)).thenReturn(Optional.of(vehicule));
        when(garageRepository.findById(3L)).thenReturn(Optional.of(garage));
        when(demandeDerogationRepository.findFirstByClientIdAndStatutOrderByDateDemandeDesc(
                client.getId(), StatutDemandeDerogationPlafond.EN_ATTENTE)).thenReturn(Optional.empty());
        when(demandeDerogationRepository.save(any(DemandeDerogationPlafond.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.bookRendezVous(client,
                new RendezVousRequest(LocalDateTime.now().plusDays(1), "Diagnostic", 77L, 3L), "awa@example.com");

        assertEquals("DEROGATION_EN_ATTENTE", result.type().name());
        assertNotNull(result.demandeDerogation());
        assertEquals("500000", result.demandeDerogation().plafondSnapshot().toPlainString());
        verify(rendezvousRepository, never()).save(any(RendezVous.class));
        verify(agentNotificationService).notifyRole(
                eq(sn.oas.facturation.features.user.data.enums.Role.SUPER_AGENT),
                eq("Dérogation de plafond à examiner"), contains("DK-7788-AA"));
    }

    @Test
    void uneCreationBackofficeRespecteAussiLePlafondEtDemandeUneDecisionSuperAgent() {
        Vehicule vehicule = Vehicule.builder().client(client).immatriculation("DK-7788-AA")
                .marque("Opel").modele("Corsa").kilometrage(1000.0).actif(true).build();
        vehicule.setId(77L);
        Garage garage = Garage.builder().nom("Dakar").localite("Dakar").prefixe("DK").build();
        garage.setId(3L);
        when(clientRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(client));
        when(vehiculeRepository.findById(77L)).thenReturn(Optional.of(vehicule));
        when(garageRepository.findById(3L)).thenReturn(Optional.of(garage));
        when(politiqueFinanciereClientService.evaluerReservation(client)).thenReturn(
                new EvaluationReservation(false, true, false, "Plafond atteint", new java.math.BigDecimal("500000"),
                        new java.math.BigDecimal("500000"), 30));
        when(demandeDerogationRepository.findFirstByClientIdAndStatutOrderByDateDemandeDesc(
                4L, StatutDemandeDerogationPlafond.EN_ATTENTE)).thenReturn(Optional.empty());
        when(demandeDerogationRepository.save(any(DemandeDerogationPlafond.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.createRendezVousByAdmin(
                new RendezVousRequest(LocalDateTime.now().plusDays(1), "Réparation", 77L, 3L, 4L,
                        RendezVousStatus.CONFIRME, "Demande prioritaire"),
                "agent@oas.sn", "AGENT");

        assertEquals("DEROGATION_EN_ATTENTE", result.type().name());
        assertEquals("AGENT", result.demandeDerogation().origine());
        assertEquals(RendezVousStatus.CONFIRME.name(), result.demandeDerogation().statutRendezVousDemande());
        verify(rendezvousRepository, never()).save(any(RendezVous.class));
        verify(agentNotificationService).notifyRole(eq(sn.oas.facturation.features.user.data.enums.Role.SUPER_AGENT),
                eq("Dérogation de plafond à examiner"), contains("DK-7788-AA"));
    }

    @Test
    void uneApprobationCreeLeRendezVousUneSeuleFoisEtTraceLaDecision() {
        Vehicule vehicule = Vehicule.builder().client(client).immatriculation("DK-7788-AA")
                .marque("Opel").modele("Corsa").kilometrage(1000.0).actif(true).build();
        vehicule.setId(77L);
        Garage garage = Garage.builder().nom("Dakar").localite("Dakar").prefixe("DK").build();
        garage.setId(3L);
        DemandeDerogationPlafond demande = DemandeDerogationPlafond.builder()
                .id(42L).client(client).vehicule(vehicule).garage(garage)
                .dateRendezVous(LocalDateTime.now().plusDays(1)).motif("Réparation")
                .statut(StatutDemandeDerogationPlafond.EN_ATTENTE)
                .statutRendezVousDemande(RendezVousStatus.EN_ATTENTE)
                .origine("CLIENT").demandeur("awa@example.com").build();
        when(demandeDerogationRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(demande));
        when(clientRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(client));
        when(politiqueFinanciereClientService.motifBlocageRetard(4L)).thenReturn(null);
        when(documentNumberGeneratorService.generateNextNumber(garage,
                sn.oas.facturation.shared.documentNumber.DocumentType.RDV)).thenReturn("RDV-DK-042");
        when(rendezvousRepository.save(any(RendezVous.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(demandeDerogationRepository.save(any(DemandeDerogationPlafond.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RendezVous created = service.approveQuotaDerogation(42L, "super-agent@oas.sn", "Urgence client validée");

        assertEquals("RDV-DK-042", created.getNumero());
        assertEquals(StatutDemandeDerogationPlafond.APPROUVEE, demande.getStatut());
        assertEquals("super-agent@oas.sn", demande.getDecisionnaire());
        assertEquals("Urgence client validée", demande.getMotifDecision());
        assertSame(created, demande.getRendezVous());
        verify(clientRepository).findByIdForUpdate(4L);
        assertThrows(BadRequestException.class,
                () -> service.approveQuotaDerogation(42L, "super-agent@oas.sn", "Nouvelle décision"));
        verify(rendezvousRepository, times(1)).save(any(RendezVous.class));
    }

    @Test
    void appointmentCannotBeChangedValidatedOrCancelledBeforeVehicleActivation() {
        Vehicule vehicule = Vehicule.builder().client(client).immatriculation("DK-7788-AA")
                .marque("Opel").modele("Corsa").kilometrage(1000.0).actif(false).build();
        rendezVous.setVehicule(vehicule);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);

        assertThrows(BadRequestException.class, () -> service.validerRendezVous(12L, java.util.List.of()));
        assertThrows(BadRequestException.class,
                () -> service.updateRendezVousStatus(12L, RendezVousStatus.CONFIRME, null));
        assertThrows(BadRequestException.class, () -> service.updateDate(12L, futureDate));
        assertThrows(BadRequestException.class,
                () -> service.updateRendezVous(12L, new RendezVousRequest(futureDate, "Diagnostic", null, null)));
        assertThrows(BadRequestException.class,
                () -> service.cancelRendezVous(client, 12L, "Demande de changement"));

        verify(rendezvousRepository, never()).save(any());
        verifyNoInteractions(notificationService, agentNotificationService);
    }

    @Test
    void rejectsReasonLongerThanOneThousandCharacters() {
        assertThrows(BadRequestException.class,
                () -> service.updateRendezVousStatus(12L, RendezVousStatus.ANNULE, null, "x".repeat(1001)));

        verify(rendezvousRepository, never()).save(any());
        verifyNoInteractions(notificationService, agentNotificationService);
    }

    @Test
    void legacyRefuseStatusUsesCancellationWorkflow() {
        when(rendezvousRepository.save(any(RendezVous.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateRendezVousStatus(12L, RendezVousStatus.REFUSE, null, "Véhicule indisponible");

        assertEquals(RendezVousStatus.ANNULE, rendezVous.getStatut());
        assertEquals("Véhicule indisponible", rendezVous.getMotifAnnulation());
        verify(notificationService).sendNotification(eq(client), eq("Rendez-vous annulé"), contains("a été annulé"));
    }

    @Test
    void clientCanOnlyCancelAppointmentStillPending() {
        rendezVous.setStatut(RendezVousStatus.CONFIRME);

        assertThrows(BadRequestException.class,
                () -> service.cancelRendezVous(client, 12L, "Je ne suis plus disponible"));

        verify(rendezvousRepository, never()).save(any());
        verifyNoInteractions(notificationService, agentNotificationService);
    }
}
