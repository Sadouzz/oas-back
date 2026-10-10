package sn.oas.facturation.features.vehicule.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.features.vehicule.dto.VehiculeRequest;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculeServicePaginationTest {
    @Mock VehiculeRepository vehiculeRepository;
    @Mock ClientRepository clientRepository;
    @InjectMocks VehiculeServiceImpl service;

    @Test void pageParClientUtiliseLaPageDemandeeEtUnTriStable() {
        when(vehiculeRepository.findAllByClientId(eq(17L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of()));

        var result = service.getVehiculesByClient(17L, 2, 10);

        assertTrue(result.isEmpty());
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(vehiculeRepository).findAllByClientId(eq(17L), pageable.capture());
        assertEquals(2, pageable.getValue().getPageNumber());
        assertEquals(10, pageable.getValue().getPageSize());
        assertEquals("id: DESC", pageable.getValue().getSort().toString());
    }

    @Test void refuseUneTailleDePageHorsLimites() {
        assertThrows(sn.oas.facturation.shared.exception.BadRequestException.class,
                () -> service.getVehiculesByClient(17L, 0, 101));
        verifyNoInteractions(vehiculeRepository);
    }

    @Test void creationClientEnregistreLeVehiculeInactifEtCreationAgentActif() {
        when(vehiculeRepository.existsByImmatriculationIgnoreCase(anyString())).thenReturn(false);
        when(clientRepository.findById(5L)).thenReturn(Optional.of(Client.builder().id(5L).build()));
        when(vehiculeRepository.save(any(Vehicule.class))).thenAnswer(invocation -> invocation.getArgument(0));
        VehiculeRequest request = new VehiculeRequest("DK-1234-AA", 2020, "Corsa", "Opel", 12000.0, null, 5L);

        assertFalse(service.createVehicule(request, false).isActif());
        assertTrue(service.createVehicule(request, true).isActif());
    }

    @Test void activationAgentRendLeVehiculeActif() {
        Vehicule vehicule = Vehicule.builder().immatriculation("DK-1234-AA").modele("Corsa")
                .marque("Opel").kilometrage(12000.0).actif(false).build();
        when(vehiculeRepository.findById(9L)).thenReturn(Optional.of(vehicule));
        when(vehiculeRepository.save(vehicule)).thenReturn(vehicule);

        assertTrue(service.activerVehicule(9L).isActif());
        verify(vehiculeRepository).save(vehicule);
    }

    @Test void refuseUneOperationSurUnVehiculeEnAttenteDAactivation() {
        Vehicule inactif = Vehicule.builder().immatriculation("DK-1234-AA").modele("Corsa")
                .marque("Opel").kilometrage(12000.0).actif(false).build();
        assertThrows(sn.oas.facturation.shared.exception.BadRequestException.class,
                () -> VehiculeActivationPolicy.requireActive(inactif));
        assertDoesNotThrow(() -> VehiculeActivationPolicy.requireActive(
                Vehicule.builder().immatriculation("DK-5678-AA").modele("Corsa")
                        .marque("Opel").kilometrage(12000.0).actif(true).build()));
    }
}
