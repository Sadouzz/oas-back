package sn.oas.facturation.features.ficheAtelier.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelierPdf;
import sn.oas.facturation.features.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.features.ficheAtelier.repository.FicheAtelierPdfRepository;
import sn.oas.facturation.features.pdfGenerator.service.HtmlToPdfService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FicheAtelierPdfServiceTest {
    @Mock FicheAtelierRepository repository;
    @Mock FicheAtelierPdfRepository pdfRepository;

    private static final String PNG = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/lV8AAAAASUVORK5CYII=";

    @Test
    void genereUnPdfAvecConditionsEtSignatures() {
        FicheAtelier fiche = FicheAtelier.builder().id(1L).numero("FA-1")
                .signatureBase64(PNG).signatureReceptionnaireBase64(PNG).build();
        when(repository.findById(1L)).thenReturn(Optional.of(fiche));
        when(pdfRepository.findById(1L)).thenReturn(Optional.empty());
        FicheAtelierPdfService service = new FicheAtelierPdfService(repository, pdfRepository, new HtmlToPdfService(null));

        byte[] pdf = service.generer(1L);

        assertTrue(pdf.length > 100);
        assertEquals("%PDF", new String(pdf, 0, 4, java.nio.charset.StandardCharsets.US_ASCII));
    }

    @Test
    void renvoieLaCopieFigeeSansRegeneration() {
        byte[] snapshot = "%PDF-snapshot".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        FicheAtelier fiche = FicheAtelier.builder().id(1L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(fiche));
        when(pdfRepository.findById(1L)).thenReturn(Optional.of(new FicheAtelierPdf(1L, snapshot)));
        FicheAtelierPdfService service = new FicheAtelierPdfService(repository, pdfRepository, new HtmlToPdfService(null));

        assertSame(snapshot, service.generer(1L));
    }
}
