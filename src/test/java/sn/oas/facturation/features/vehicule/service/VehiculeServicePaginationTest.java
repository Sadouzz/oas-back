package sn.oas.facturation.features.vehicule.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
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
}
