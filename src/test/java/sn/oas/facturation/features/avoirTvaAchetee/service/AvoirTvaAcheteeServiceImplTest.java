package sn.oas.facturation.features.avoirTvaAchetee.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import sn.oas.facturation.features.avoirTvaAchetee.data.entity.AvoirTvaAchetee;
import sn.oas.facturation.features.avoirTvaAchetee.dto.AvoirTvaAcheteeRequest;
import sn.oas.facturation.features.avoirTvaAchetee.repository.AvoirTvaAcheteeRepository;
import sn.oas.facturation.features.user.repository.UserRepository;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvoirTvaAcheteeServiceImplTest {

    @Mock AvoirTvaAcheteeRepository repository;
    @Mock UserRepository userRepository;

    @InjectMocks AvoirTvaAcheteeServiceImpl service;

    @Test
    void creeListeParMoisEtSupprimeAvoirTvaAchetee() {
        when(repository.save(any(AvoirTvaAchetee.class))).thenAnswer(inv -> {
            AvoirTvaAchetee a = inv.getArgument(0);
            a.setId(20L);
            return a;
        });

        AvoirTvaAchetee created = service.create(new AvoirTvaAcheteeRequest(
                " Remise fournisseur ",
                LocalDate.of(2026, 11, 15),
                new BigDecimal("1500")));

        assertEquals(20L, created.getId());
        assertEquals("Remise fournisseur", created.getDesignation());
        assertEquals(new BigDecimal("1500.00"), created.getMontantTva());

        YearMonth nov = YearMonth.of(2026, 11);
        when(repository.findByDateOperationBetweenOrderByDateOperationDescIdDesc(
                nov.atDay(1), nov.atEndOfMonth(), PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(created)));

        var page = service.getByMonth(nov, 0, 10);
        assertEquals(1, page.getTotalElements());

        when(repository.findById(20L)).thenReturn(Optional.of(created));
        service.delete(20L);
        verify(repository).delete(created);
    }

    @Test
    void rejetteDonneesInvalidesOuIdInconnu() {
        assertThrows(BadRequestException.class, () ->
                service.create(new AvoirTvaAcheteeRequest("", LocalDate.now(), BigDecimal.TEN)));
        assertThrows(BadRequestException.class, () ->
                service.create(new AvoirTvaAcheteeRequest("Avoir", LocalDate.now(), new BigDecimal("-5"))));

        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.delete(99L));
    }
}
