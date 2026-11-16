package sn.oas.facturation.features.client.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.repository.FactureRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolitiqueFinanciereClientServiceTest {
    @Mock FactureRepository factureRepository;
    @Mock ClientRepository clientRepository;
    @InjectMocks PolitiqueFinanciereClientService politique;
    private Client client;

    @BeforeEach void setUp() {
        client = Client.builder().id(7L).echeance(30)
                .montantPlafondEcheance(new BigDecimal("500000")).build();
        lenient().when(factureRepository.sumResteAPayerByClientId(7L)).thenReturn(new BigDecimal("100000"));
        lenient().when(factureRepository.findFacturesImpayeesEnRetard(eq(7L), any(), any())).thenReturn(List.of());
    }

    @Test void encoursImpayesSousLePlafondAutoriseUnNouveauWorkflow() {
        when(clientRepository.findById(7L)).thenReturn(Optional.of(client));

        var evaluation = politique.evaluerReservation(7L);

        assertTrue(evaluation.workflowAutorise());
        assertFalse(evaluation.plafondAtteint());
        assertFalse(evaluation.factureEchueImpayee());
        assertEquals(new BigDecimal("100000"), evaluation.encoursImpayes());
        verify(factureRepository, never()).sumMontantFacturesDepuis(anyLong(), any());
    }

    @Test void encoursEgalOuSuperieurAuPlafondBloqueLeNouveauWorkflow() {
        when(clientRepository.findById(7L)).thenReturn(Optional.of(client));
        when(factureRepository.sumResteAPayerByClientId(7L))
                .thenReturn(new BigDecimal("500000"), new BigDecimal("500001"));

        var evaluationAuPlafond = politique.evaluerReservation(7L);
        assertFalse(evaluationAuPlafond.workflowAutorise());
        assertTrue(evaluationAuPlafond.plafondAtteint());
        assertTrue(evaluationAuPlafond.motifBlocage().contains("500000 / 500000"));

        var evaluationAuDessus = politique.evaluerReservation(7L);

        assertFalse(evaluationAuDessus.workflowAutorise());
        assertTrue(evaluationAuDessus.plafondAtteint());
        assertFalse(evaluationAuDessus.factureEchueImpayee());
        assertTrue(evaluationAuDessus.motifBlocage().contains("500001 / 500000"));
    }

    @Test void toutesLesFacturesEchuesBloqueusesSontExposeesAvecLeurResteDu() {
        when(clientRepository.findById(7L)).thenReturn(Optional.of(client));
        when(factureRepository.sumResteAPayerByClientId(7L)).thenReturn(new BigDecimal("200000"));
        LocalDateTime now = LocalDateTime.now();
        List<Facture> factures = List.of(
                facture("FAC-301", now.minusDays(45), now.minusDays(15), "50000"),
                facture("FAC-302", now.minusDays(40), now.minusDays(10), "70000"),
                facture("FAC-303", now.minusDays(35), now.minusDays(5), "80000"));
        when(factureRepository.findFacturesImpayeesEnRetard(eq(7L), any(), any())).thenReturn(factures);

        var evaluation = politique.evaluerReservation(7L);

        assertFalse(evaluation.workflowAutorise());
        assertFalse(evaluation.plafondAtteint());
        assertTrue(evaluation.factureEchueImpayee());
        assertEquals(3, evaluation.facturesEchuesImpayees().size());
        assertTrue(evaluation.motifBlocage().contains("FAC-301"));
        assertTrue(evaluation.motifBlocage().contains("FAC-302"));
        assertTrue(evaluation.motifBlocage().contains("FAC-303"));
        assertTrue(evaluation.motifBlocage().contains("80000 F CFA"));
    }

    @Test void cumulDuPlafondEtFacturesEchuesSontSignalesEnsemble() {
        when(clientRepository.findById(7L)).thenReturn(Optional.of(client));
        when(factureRepository.sumResteAPayerByClientId(7L)).thenReturn(new BigDecimal("600000"));
        when(factureRepository.findFacturesImpayeesEnRetard(eq(7L), any(), any()))
                .thenReturn(List.of(facture("FAC-401", LocalDateTime.now().minusDays(60),
                        LocalDateTime.now().minusDays(30), "600000")));

        var evaluation = politique.evaluerReservation(7L);

        assertTrue(evaluation.plafondAtteint());
        assertTrue(evaluation.factureEchueImpayee());
        assertTrue(evaluation.motifBlocage().contains("plafond"));
        assertTrue(evaluation.motifBlocage().contains("FAC-401"));
        assertTrue(evaluation.derogationPossible());
    }

    @Test void proformaAvertitSurLEncoursProjeteMaisNeBloquePasLeParcoursDejaCommence() {
        when(factureRepository.sumResteAPayerByClientId(7L)).thenReturn(new BigDecimal("100000"));

        var warnings = politique.avertissementsProforma(client, new BigDecimal("400000"));

        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("encours impayé"));
        assertTrue(warnings.get(0).contains("reste autorisée"));
        verify(factureRepository, never()).sumMontantFacturesDepuis(anyLong(), any());
    }

    private Facture facture(String numero, LocalDateTime emission, LocalDateTime echeance, String reste) {
        return Facture.builder()
                .numero(numero)
                .dateCreation(emission)
                .dateEcheance(echeance)
                .montantTotal(new BigDecimal(reste))
                .montantPaye(BigDecimal.ZERO)
                .resteAPayer(new BigDecimal(reste))
                .build();
    }
}
