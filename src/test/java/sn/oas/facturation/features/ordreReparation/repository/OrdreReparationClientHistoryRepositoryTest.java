package sn.oas.facturation.features.ordreReparation.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrdreReparationClientHistoryRepositoryTest {

    @Autowired
    private OrdreReparationRepository ordreReparationRepository;

    @Test
    void clientHistoryQueryReturnsAnEmptyPageForClientWithoutOrders() {
        var page = ordreReparationRepository.findClientHistoryPage(Long.MAX_VALUE, PageRequest.of(0, 8));

        assertTrue(page.isEmpty());
        assertTrue(page.isFirst());
    }
}
