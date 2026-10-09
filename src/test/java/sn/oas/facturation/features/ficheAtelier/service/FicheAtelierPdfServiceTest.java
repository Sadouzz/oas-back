package sn.oas.facturation.features.ficheAtelier.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelierPdf;
import sn.oas.facturation.features.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.features.ficheAtelier.repository.FicheAtelierPdfRepository;
import sn.oas.facturation.features.pdfGenerator.service.HtmlToPdfService;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FicheAtelierPdfServiceTest {
    @Mock FicheAtelierRepository repository;
    @Mock FicheAtelierPdfRepository pdfRepository;

    private static final String PNG = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAlgAAADICAYAAAA0n5+2AAAEYUlEQVR42u3dS27jQBBEQd7/0vZegGGSyv5VRQDazsAkm/lGC891AQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAn+3EJAAByYfX5AQAgGFdCCwBgUFyJLACAQYElsgAAwnEltAAABsWVyAIAGBBXQgsA4GFgPY0wAABxdTOWfJsFABCMq7ehBQAgrm6GkcgCAHgYR8k/S2gBAO0Da0SwiSwAQFwJLQA4Y9ipG1ciCwAWjzo140pkAUChQWfP+yG0AGDysNIjdkUWABhUceW5AIAzw8qQ9oorkQUAk+LKkK6/Jyc8KwBgxA3oMffmpGcHAAz4HwNpQMWVyAKA8DAaT3EltABgwBAaTnElsgAgPIBGU2AJLQAYMHgGU1yJLACMdnjojKW4ElkAGO0BA2csxZXQAsBghwfNSM67V55JAGg0ZEZy/P3yfAJAs+EykK6fyALAYE3+uxFXiWfWswRAu28DDKNrdvLzCwDbDpNRFFgiCwCDJLLElecaAPYfIGMorkQWAIZHZIkrzzsAxmb/sTGC7+8fIgsAAyOygvcRkQWAYRFY4sp5AMCQiCxx5WwAYEAOHxDDJwCcEwCMhsgSWM4LAIZCYIkr/H+G8P95AAei4Dh0HDsj7xzBqn9EePZxaJocim4H3gvOmYJZwSSwoPkQdDnwXnDOF5653T/gIDb6mf2cOGcIJnEFXvjhn19c4dwhmgQWeMmLLHHl/CGahBV4sQssceUsOouiSUiBF7rIElg4l4LJB/AS7xdY7q3z6Rr6CCbw4hZZ4grnVTCJJvCyFljiCudWNAkm8IJ2aMtHlvvs2xvRJJoAcSWwfHvF5udZ/IgmQFy1jiz32bm+c7+Fj2gChJXAEleIJMEEiCuRJa4QWaIJEFYcHy/uuWdUMAGIq1LXWFwhskQTIK4oFDHuObtFFoCwInbNxRWVQwvAy9OLsUVkufd885wAYFwF1otnAAAQVyJLXAFA74F3/dddf3EFABNHlvqRJa4AYNLII7AEFgAgssQVAMDawBJXAIDICoaPuAIABFYwfvxKDgBAZIUDSFwBAAIrGELiCgAQWcEYElcAAMEoElcAADfjSGABACyILHEFABAMLHEFABCMLHEFABAMLL9MFAAgHFniCgAgGFjiCgAgGFniCgBgYmABABCOLAAAgoEFAEAwsgAACAYWAADhyAIAIBhYAAAEIwsAgFBoAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA8MwvValaa5xb9jwAAAAASUVORK5CYII=";

    @Test
    void genereUnPdfAvecConditionsEtSignatures() throws Exception {
        FicheAtelier fiche = FicheAtelier.builder().id(1L).numero("FA-1")
                .signatureBase64(PNG).signatureReceptionnaireBase64(PNG)
                .lignesReception(List.of(
                        new sn.oas.facturation.features.ficheAtelier.data.entity.LigneReception("Voyants allumés", true, false),
                        new sn.oas.facturation.features.ficheAtelier.data.entity.LigneReception("Climatisation", true, false),
                        new sn.oas.facturation.features.ficheAtelier.data.entity.LigneReception("Phares", true, false),
                        new sn.oas.facturation.features.ficheAtelier.data.entity.LigneReception("Feux", true, false),
                        new sn.oas.facturation.features.ficheAtelier.data.entity.LigneReception("Poste radio", true, false),
                        new sn.oas.facturation.features.ficheAtelier.data.entity.LigneReception("Rétroviseurs", true, false),
                        new sn.oas.facturation.features.ficheAtelier.data.entity.LigneReception("Klaxon", true, false),
                        new sn.oas.facturation.features.ficheAtelier.data.entity.LigneReception("Lève-vitres", true, false),
                        new sn.oas.facturation.features.ficheAtelier.data.entity.LigneReception("Pare-brise", true, false),
                        new sn.oas.facturation.features.ficheAtelier.data.entity.LigneReception("Lunette arrière", true, false)))
                .lignesDefauts(List.of(
                        new sn.oas.facturation.features.ficheAtelier.data.entity.LigneDefaut("Mécanique", false, "RAS", false),
                        new sn.oas.facturation.features.ficheAtelier.data.entity.LigneDefaut("Électrique", false, "RAS", false),
                        new sn.oas.facturation.features.ficheAtelier.data.entity.LigneDefaut("Climatisation", false, "RAS", false),
                        new sn.oas.facturation.features.ficheAtelier.data.entity.LigneDefaut("Peinture", false, "RAS", false),
                        new sn.oas.facturation.features.ficheAtelier.data.entity.LigneDefaut("Tôlerie", false, "RAS", false)))
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(fiche));
        when(pdfRepository.findById(1L)).thenReturn(Optional.empty());
        FicheAtelierPdfService service = new FicheAtelierPdfService(repository, pdfRepository, new HtmlToPdfService(null));

        byte[] pdf = service.generer(1L);

        assertTrue(pdf.length > 100);
        assertEquals("%PDF", new String(pdf, 0, 4, java.nio.charset.StandardCharsets.US_ASCII));
        PdfReader reader = new PdfReader(pdf);
        assertEquals(2, reader.getNumberOfPages(), "La fiche et les conditions doivent tenir chacune sur une page.");
        PdfTextExtractor textExtractor = new PdfTextExtractor(reader);
        assertTrue(textExtractor.getTextFromPage(1).contains("Fiche Atelier"));
        assertFalse(textExtractor.getTextFromPage(1).contains("Article 1er"));
        assertTrue(textExtractor.getTextFromPage(2).contains("Conditions générales de réparation"));
        assertTrue(textExtractor.getTextFromPage(2).contains("Signatures à la réception"));
        var resources = reader.getPageN(2).getAsDict(PdfName.RESOURCES);
        assertNotNull(resources.getAsDict(PdfName.XOBJECT), "Les signatures doivent être intégrées comme images.");
        assertFalse(resources.getAsDict(PdfName.XOBJECT).getKeys().isEmpty());
        reader.close();
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
