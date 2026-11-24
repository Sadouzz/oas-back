package sn.oas.facturation.features.tva.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import sn.oas.facturation.features.avoirTTC.data.entity.AvoirTTC;
import sn.oas.facturation.features.avoirTTC.repository.AvoirTTCRepository;
import sn.oas.facturation.features.avoirTvaAchetee.repository.AvoirTvaAcheteeRepository;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.factureAchetee.data.entity.FactureAchetee;
import sn.oas.facturation.features.factureAchetee.repository.FactureAcheteeRepository;
import sn.oas.facturation.features.pdfGenerator.service.HtmlToPdfService;
import sn.oas.facturation.features.tva.data.entity.RapportTvaMensuel;
import sn.oas.facturation.features.tva.dto.TvaDeclarationResponseDto;
import sn.oas.facturation.features.tva.dto.TvaRapportUpdateRequest;
import sn.oas.facturation.features.tva.repository.RapportTvaMensuelRepository;
import sn.oas.facturation.shared.exception.BadRequestException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TvaDeclarationServiceImplTest {

    @Mock FactureRepository factureRepository;
    @Mock AvoirTTCRepository avoirTTCRepository;
    @Mock FactureAcheteeRepository factureAcheteeRepository;
    @Mock AvoirTvaAcheteeRepository avoirTvaAcheteeRepository;
    @Mock RapportTvaMensuelRepository rapportRepository;

    private TvaDeclarationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TvaDeclarationServiceImpl(
                factureRepository,
                avoirTTCRepository,
                factureAcheteeRepository,
                avoirTvaAcheteeRepository,
                rapportRepository,
                new HtmlToPdfService(null));
    }

    @Test
    void calculeSyntheseSauvegardeCreditNegatifEtReporteAuMoisSuivant() {
        YearMonth nov = YearMonth.of(2026, 11);
        LocalDate debutNov = nov.atDay(1);
        LocalDate finNov = nov.atEndOfMonth();
        LocalDateTime dtDebutNov = debutNov.atStartOfDay();
        LocalDateTime dtFinNov = finNov.plusDays(1).atStartOfDay();

        Facture facture = Facture.builder()
                .id(1L)
                .numero("3259")
                .dateCreation(LocalDateTime.of(2026, 11, 13, 0, 49, 33))
                .montantTVA(new BigDecimal("45"))
                .montantTimbre(BigDecimal.ZERO)
                .build();

        AvoirTTC avoir = AvoirTTC.builder()
                .id(2L)
                .numero("57")
                .dateCreation(LocalDateTime.of(2026, 11, 13, 17, 46, 34))
                .montantTVA(new BigDecimal("45"))
                .montantTimbre(BigDecimal.ZERO)
                .build();

        FactureAchetee fa = FactureAchetee.builder()
                .id(3L)
                .numeroFacture("vgjbh & cie")
                .dateFacture(LocalDate.of(2026, 11, 24))
                .montantTva(new BigDecimal("5000"))
                .build();

        when(factureRepository.findByDateCreationGreaterThanEqualAndDateCreationLessThanOrderByDateCreationDescIdDesc(
                dtDebutNov, dtFinNov, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(facture)));
        when(avoirTTCRepository.findByDateCreationGreaterThanEqualAndDateCreationLessThanOrderByDateCreationDescIdDesc(
                dtDebutNov, dtFinNov, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(avoir)));
        when(factureAcheteeRepository.findByDateFactureBetweenOrderByDateFactureDescIdDesc(
                debutNov, finNov, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(fa)));
        when(avoirTvaAcheteeRepository.findByDateOperationBetweenOrderByDateOperationDescIdDesc(
                debutNov, finNov, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of()));

        when(factureRepository.sumMontantTvaBetween(dtDebutNov, dtFinNov)).thenReturn(new BigDecimal("45"));
        when(factureRepository.sumMontantTimbreBetween(dtDebutNov, dtFinNov)).thenReturn(BigDecimal.ZERO);
        when(avoirTTCRepository.sumMontantTvaBetween(dtDebutNov, dtFinNov)).thenReturn(new BigDecimal("45"));
        when(avoirTTCRepository.sumMontantTimbreBetween(dtDebutNov, dtFinNov)).thenReturn(BigDecimal.ZERO);
        when(factureAcheteeRepository.sumMontantTvaBetween(debutNov, finNov)).thenReturn(new BigDecimal("5000"));
        when(avoirTvaAcheteeRepository.sumMontantTvaBetween(debutNov, finNov)).thenReturn(BigDecimal.ZERO);
        when(rapportRepository.findFirstByPeriodeOrderByIdDesc("2026-10")).thenReturn(Optional.empty());
        when(rapportRepository.findFirstByPeriodeOrderByIdDesc("2026-11")).thenReturn(Optional.empty());

        TvaDeclarationResponseDto resNov = service.getDeclaration(nov, 0, 0, 0, 0, 10);

        assertEquals("2026-11", resNov.getMonth());
        assertEquals(new BigDecimal("45"), resNov.getTotalTvaVendue());
        assertEquals(new BigDecimal("45"), resNov.getTotalTvaAvoirVendu());
        assertEquals(BigDecimal.ZERO, resNov.getRapportTvaVendue());
        assertEquals(new BigDecimal("5000"), resNov.getTotalTvaAchetee());
        assertEquals(new BigDecimal("5000"), resNov.getRapportTvaAchetee());
        assertEquals(BigDecimal.ZERO, resNov.getRapport());
        assertEquals(new BigDecimal("-5000"), resNov.getDifference());
        assertEquals(new BigDecimal("-5000"), resNov.getSolde());

        ArgumentCaptor<RapportTvaMensuel> captor = ArgumentCaptor.forClass(RapportTvaMensuel.class);
        verify(rapportRepository).save(captor.capture());
        assertEquals("2026-11", captor.getValue().getPeriode());
        assertEquals(new BigDecimal("-5000.00"), captor.getValue().getMontant());

        // Génération PDF réelle avec Flying Saucer (y compris caractère '&' échappé en XHTML)
        when(factureRepository.findByDateCreationGreaterThanEqualAndDateCreationLessThanOrderByDateCreationDescIdDesc(
                dtDebutNov, dtFinNov)).thenReturn(List.of(facture));
        when(avoirTTCRepository.findByDateCreationGreaterThanEqualAndDateCreationLessThanOrderByDateCreationDescIdDesc(
                dtDebutNov, dtFinNov)).thenReturn(List.of(avoir));
        when(factureAcheteeRepository.findByDateFactureBetweenOrderByDateFactureDescIdDesc(debutNov, finNov))
                .thenReturn(List.of(fa));
        when(avoirTvaAcheteeRepository.findByDateOperationBetweenOrderByDateOperationDescIdDesc(debutNov, finNov))
                .thenReturn(List.of());
        when(rapportRepository.findFirstByPeriodeOrderByIdDesc("2026-11"))
                .thenReturn(Optional.of(captor.getValue()));

        byte[] pdf = service.generateDeclarationPdf(nov);
        assertNotNull(pdf);
        assertTrue(pdf.length > 100);
        assertEquals("%PDF-", new String(pdf, 0, 5, StandardCharsets.US_ASCII));

        // Mois suivant (2026-12) : le crédit de -5000 est repris dans rapport
        YearMonth dec = YearMonth.of(2026, 12);
        LocalDate debutDec = dec.atDay(1);
        LocalDate finDec = dec.atEndOfMonth();
        LocalDateTime dtDebutDec = debutDec.atStartOfDay();
        LocalDateTime dtFinDec = finDec.plusDays(1).atStartOfDay();

        when(factureRepository.findByDateCreationGreaterThanEqualAndDateCreationLessThanOrderByDateCreationDescIdDesc(
                dtDebutDec, dtFinDec, PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of()));
        when(avoirTTCRepository.findByDateCreationGreaterThanEqualAndDateCreationLessThanOrderByDateCreationDescIdDesc(
                dtDebutDec, dtFinDec, PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of()));
        when(factureAcheteeRepository.findByDateFactureBetweenOrderByDateFactureDescIdDesc(
                debutDec, finDec, PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of()));
        when(avoirTvaAcheteeRepository.findByDateOperationBetweenOrderByDateOperationDescIdDesc(
                debutDec, finDec, PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of()));

        when(factureRepository.sumMontantTvaBetween(dtDebutDec, dtFinDec)).thenReturn(new BigDecimal("12000"));
        when(factureRepository.sumMontantTimbreBetween(dtDebutDec, dtFinDec)).thenReturn(new BigDecimal("500"));
        when(avoirTTCRepository.sumMontantTvaBetween(dtDebutDec, dtFinDec)).thenReturn(BigDecimal.ZERO);
        when(avoirTTCRepository.sumMontantTimbreBetween(dtDebutDec, dtFinDec)).thenReturn(BigDecimal.ZERO);
        when(factureAcheteeRepository.sumMontantTvaBetween(debutDec, finDec)).thenReturn(new BigDecimal("2000"));
        when(avoirTvaAcheteeRepository.sumMontantTvaBetween(debutDec, finDec)).thenReturn(new BigDecimal("500"));
        when(rapportRepository.findFirstByPeriodeOrderByIdDesc("2026-12")).thenReturn(Optional.empty());

        TvaDeclarationResponseDto resDec = service.getDeclaration(dec, 0, 0, 0, 0, 10);
        // Rapport TVA vendue = 12000, Rapport TVA achetée = 2000 - 500 = 1500 => Différence = 10500
        // Solde = 10500 + (-5000) = 5500
        assertEquals(new BigDecimal("-5000.00"), resDec.getRapport());
        assertEquals(new BigDecimal("10500"), resDec.getDifference());
        assertEquals(new BigDecimal("5500.00"), resDec.getSolde());
        assertEquals(new BigDecimal("500"), resDec.getTotalTimbre());
    }

    @Test
    void rejetteReportPositifLorsMiseAJourManuelle() {
        assertThrows(BadRequestException.class, () ->
                service.updateRapport(new TvaRapportUpdateRequest("2026-10", new BigDecimal("100"), true)));
    }
}
