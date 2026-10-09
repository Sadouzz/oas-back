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
import sn.oas.facturation.features.garage.repository.GarageRepository;
import sn.oas.facturation.features.notification.service.EmailService;
import sn.oas.facturation.features.notification.service.NotificationService;
import sn.oas.facturation.features.ordreReparation.service.OrdreReparationService;
import sn.oas.facturation.features.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.features.rendezvous.data.enums.RendezVousStatus;
import sn.oas.facturation.features.rendezvous.repository.RendezVousRepository;
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
    @Mock OrdreReparationService ordreReparationService;
    @Mock DocumentNumberGeneratorService documentNumberGeneratorService;
    @Mock EmailService emailService;
    @Mock PolitiqueFinanciereClientService politiqueFinanciereClientService;

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
        when(rendezvousRepository.findById(12L)).thenReturn(Optional.of(rendezVous));
    }

    @Test
    void refusesCancellationWithoutReason() {
        assertThrows(BadRequestException.class,
                () -> service.updateRendezVousStatus(12L, RendezVousStatus.ANNULE, null, " "));

        verify(rendezvousRepository, never()).save(any());
        verifyNoInteractions(notificationService, emailService);
    }

    @Test
    void cancellationPersistsReasonAndNotifiesClientInAppAndByEmail() {
        when(rendezvousRepository.save(any(RendezVous.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.updateRendezVousStatus(12L, RendezVousStatus.ANNULE, null, "Client indisponible");

        assertEquals("Client indisponible", rendezVous.getMotifAnnulation());
        verify(notificationService).sendNotification(eq(client), eq("Rendez-vous annulé"),
                contains("Votre rendez-vous prévu à la date du 12/10/2026 à 09:30 a été annulé pour motif de « Client indisponible »"));
        verify(emailService).sendSimpleEmail(eq("awa@example.com"), eq("Rendez-vous annulé"),
                contains("motif de « Client indisponible »"));
    }

    @Test
    void rejectsReasonLongerThanOneThousandCharacters() {
        assertThrows(BadRequestException.class,
                () -> service.updateRendezVousStatus(12L, RendezVousStatus.ANNULE, null, "x".repeat(1001)));

        verify(rendezvousRepository, never()).save(any());
        verifyNoInteractions(notificationService, emailService);
    }

    @Test
    void legacyRefuseStatusUsesCancellationWorkflow() {
        when(rendezvousRepository.save(any(RendezVous.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateRendezVousStatus(12L, RendezVousStatus.REFUSE, null, "Véhicule indisponible");

        assertEquals(RendezVousStatus.ANNULE, rendezVous.getStatut());
        assertEquals("Véhicule indisponible", rendezVous.getMotifAnnulation());
        verify(notificationService).sendNotification(eq(client), eq("Rendez-vous annulé"), contains("a été annulé"));
        verify(emailService).sendSimpleEmail(eq("awa@example.com"), eq("Rendez-vous annulé"), contains("a été annulé"));
    }

    @Test
    void clientCanOnlyCancelAppointmentStillPending() {
        rendezVous.setStatut(RendezVousStatus.CONFIRME);

        assertThrows(BadRequestException.class,
                () -> service.cancelRendezVous(client, 12L, "Je ne suis plus disponible"));

        verify(rendezvousRepository, never()).save(any());
        verifyNoInteractions(notificationService, emailService);
    }
}
