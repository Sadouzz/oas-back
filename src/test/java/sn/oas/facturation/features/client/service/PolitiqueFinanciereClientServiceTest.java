package sn.oas.facturation.features.client.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolitiqueFinanciereClientServiceTest {
    @Mock FactureRepository factureRepository;
    @Mock ClientRepository clientRepository;
    @InjectMocks PolitiqueFinanciereClientService politique;
    private Client client;

    @BeforeEach void setUp() {
        client = Client.builder().id(7L).montantPlafond(150_000).echeance(30)
                .montantPlafondEcheance(new BigDecimal("500000")).build();
        when(factureRepository.sumResteAPayerByClientId(7L)).thenReturn(new BigDecimal("140000"));
        lenient().when(factureRepository.sumMontantFacturesDepuis(eq(7L), any(LocalDateTime.class))).thenReturn(new BigDecimal("100000"));
        lenient().when(factureRepository.existsFactureImpayeeEnRetard(eq(7L), any(LocalDateTime.class))).thenReturn(false);
        lenient().when(factureRepository.existsFactureLegacyImpayeeEnRetard(eq(7L), any(LocalDateTime.class))).thenReturn(false);
    }

    @Test void proformaAuDessusDuPlafondAvertitSansRefuser() {
        var warnings = politique.avertissementsProforma(client, new BigDecimal("30000"));
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("reste autorisée"));
    }

    @Test void reservationClientEstBloqueeSiEncoursDepassePlafondMaisPasQuandIlEstEgal() {
        when(clientRepository.findById(7L)).thenReturn(Optional.of(client));
        assertNull(politique.motifBlocageReservation(7L));
        when(factureRepository.sumResteAPayerByClientId(7L)).thenReturn(new BigDecimal("150001"));
        assertNotNull(politique.motifBlocageReservation(7L));
    }

    @Test void reservationClientBloqueeSiFactureEchueOuPlafondPeriodeAtteint() {
        when(clientRepository.findById(7L)).thenReturn(Optional.of(client));
        when(factureRepository.existsFactureImpayeeEnRetard(eq(7L), any(LocalDateTime.class))).thenReturn(true);
        assertTrue(politique.motifBlocageReservation(7L).contains("échue"));

        when(factureRepository.existsFactureImpayeeEnRetard(eq(7L), any(LocalDateTime.class))).thenReturn(false);
        when(factureRepository.sumMontantFacturesDepuis(eq(7L), any(LocalDateTime.class))).thenReturn(new BigDecimal("500000"));
        assertTrue(politique.motifBlocageReservation(7L).contains("plafond de facturation"));
    }
}
