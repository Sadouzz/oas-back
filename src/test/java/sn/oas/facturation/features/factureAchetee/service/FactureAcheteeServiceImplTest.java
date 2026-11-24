package sn.oas.facturation.features.factureAchetee.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import sn.oas.facturation.features.factureAchetee.data.entity.FactureAchetee;
import sn.oas.facturation.features.factureAchetee.dto.FactureAcheteeRequest;
import sn.oas.facturation.features.factureAchetee.repository.FactureAcheteeRepository;
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
class FactureAcheteeServiceImplTest {

    @Mock FactureAcheteeRepository repository;
    @Mock UserRepository userRepository;

    @InjectMocks FactureAcheteeServiceImpl service;

    @Test
    void creeListeParMoisEtSupprimeFactureAchetee() {
        when(repository.save(any(FactureAchetee.class))).thenAnswer(inv -> {
            FactureAchetee f = inv.getArgument(0);
            f.setId(10L);
            return f;
        });

        FactureAchetee created = service.create(new FactureAcheteeRequest(
                " TOP PNEUS N°2025/960 ",
                LocalDate.of(2025, 4, 22),
                new BigDecimal("15077")));

        assertEquals(10L, created.getId());
        assertEquals("TOP PNEUS N°2025/960", created.getNumeroFacture());
        assertEquals(new BigDecimal("15077.00"), created.getMontantTva());

        YearMonth avril = YearMonth.of(2025, 4);
        when(repository.findByDateFactureBetweenOrderByDateFactureDescIdDesc(
                avril.atDay(1), avril.atEndOfMonth(), PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(created)));

        var page = service.getByMonth(avril, 0, 10);
        assertEquals(1, page.getTotalElements());

        when(repository.findById(10L)).thenReturn(Optional.of(created));
        service.delete(10L);
        verify(repository).delete(created);
    }

    @Test
    void rejetteDonneesInvalidesOuIdInconnu() {
        assertThrows(BadRequestException.class, () ->
                service.create(new FactureAcheteeRequest(" ", LocalDate.now(), BigDecimal.TEN)));
        assertThrows(BadRequestException.class, () ->
                service.create(new FactureAcheteeRequest("F-1", LocalDate.now(), new BigDecimal("-1"))));

        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.delete(99L));
    }
}
