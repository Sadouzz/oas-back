package sn.oas.facturation.features.facture.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FactureRepositoryFinancialQueriesTest {

    @Autowired FactureRepository factureRepository;

    @Test
    void financialQueriesExecuteAgainstJpaAndReturnEmptyForClientWithoutInvoices() {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal encours = factureRepository.sumResteAPayerByClientId(Long.MAX_VALUE);
        var enRetard = factureRepository.findFacturesImpayeesEnRetard(
                Long.MAX_VALUE, now, now.minusDays(30));

        assertEquals(0, BigDecimal.ZERO.compareTo(encours));
        assertTrue(enRetard.isEmpty());
    }
}
