package sn.oas.facturation.devisPrevisionnel.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.oas.facturation.features.auth.service.AuthService;
import sn.oas.facturation.features.user.service.UserService;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.devisPrevisionnel.data.entity.DevisPrevisionnel;
import sn.oas.facturation.features.devisPrevisionnel.repository.DevisPrevisionnelRepository;
import sn.oas.facturation.features.devisPrevisionnel.service.DevisPrevisionnelServiceImpl;
import sn.oas.facturation.features.facturation.data.enums.StatutFacturation;
import sn.oas.facturation.features.vehicule.service.VehiculeService;
import sn.oas.facturation.features.notification.service.AgentNotificationService;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.user.data.enums.Role;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevisPrevisionnelServiceImplTest {

    @Mock
    private DevisPrevisionnelRepository devisPrevisionnelRepository;

    @Mock
    private VehiculeService vehiculeService;

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @Mock
    private AgentNotificationService agentNotificationService;

    @InjectMocks
    private DevisPrevisionnelServiceImpl devisPrevisionnelService;

    @Test
    void getClientDevis_shouldReturnClientDevisOrdered() {
        Client client = Client.builder().id(10L).build();
        DevisPrevisionnel devis = DevisPrevisionnel.builder().id(1L).client(client).build();
        when(devisPrevisionnelRepository.findByClientIdOrderByUpdatedAtDesc(10L)).thenReturn(List.of(devis));

        List<DevisPrevisionnel> result = devisPrevisionnelService.getClientDevis(client);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(devisPrevisionnelRepository).findByClientIdOrderByUpdatedAtDesc(10L);
    }

    @Test
    void clientAccepter_shouldSetAcceptedStatus() {
        Client client = Client.builder().id(55L).build();
        Garage garage = new Garage();
        garage.setId(12L);
        DevisPrevisionnel devis = DevisPrevisionnel.builder()
                .id(7L)
                .client(client)
                .garage(garage)
                .statut(StatutFacturation.EN_ATTENTE)
                .build();
        when(devisPrevisionnelRepository.findById(7L)).thenReturn(Optional.of(devis));
        when(devisPrevisionnelRepository.save(any(DevisPrevisionnel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DevisPrevisionnel result = devisPrevisionnelService.clientAccepter(client, 7L);

        assertThat(result.getStatut()).isEqualTo(StatutFacturation.ACCEPTE);
        verify(devisPrevisionnelRepository).save(devis);
        verify(agentNotificationService).notifyRoleInGarage(eq(Role.AGENT), eq(garage), eq("Devis prévisionnel accepté"), any());
        verify(agentNotificationService).notifyRoleInGarage(eq(Role.CHEF_ATELIER), eq(garage), eq("Devis prévisionnel accepté"), any());
    }

    @Test
    void clientRefuser_shouldSetRejectedStatus() {
        Client client = Client.builder().id(66L).build();
        Garage garage = new Garage();
        garage.setId(13L);
        DevisPrevisionnel devis = DevisPrevisionnel.builder()
                .id(8L)
                .client(client)
                .garage(garage)
                .statut(StatutFacturation.EN_ATTENTE)
                .build();
        when(devisPrevisionnelRepository.findById(8L)).thenReturn(Optional.of(devis));
        when(devisPrevisionnelRepository.save(any(DevisPrevisionnel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DevisPrevisionnel result = devisPrevisionnelService.clientRefuser(client, 8L);

        assertThat(result.getStatut()).isEqualTo(StatutFacturation.REJETE);
        verify(devisPrevisionnelRepository).save(devis);
        verify(agentNotificationService).notifyRoleInGarage(eq(Role.AGENT), eq(garage), eq("Devis prévisionnel refusé"), any());
        verify(agentNotificationService).notifyRoleInGarage(eq(Role.CHEF_ATELIER), eq(garage), eq("Devis prévisionnel refusé"), any());
    }
}
