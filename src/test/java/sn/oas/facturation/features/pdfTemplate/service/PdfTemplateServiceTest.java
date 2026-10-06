package sn.oas.facturation.features.pdfTemplate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import sn.oas.facturation.features.pdfTemplate.data.dto.PdfBlockRequest;
import sn.oas.facturation.features.pdfTemplate.data.dto.PdfTemplateWriteRequest;
import sn.oas.facturation.features.pdfTemplate.data.entity.PdfTemplate;
import sn.oas.facturation.features.pdfTemplate.repository.PdfTemplateRepository;
import sn.oas.facturation.shared.exception.BadRequestException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PdfTemplateServiceTest {
    private final PdfTemplateRepository repository = mock(PdfTemplateRepository.class);
    private final PdfTemplateService service = new PdfTemplateService(repository, new ObjectMapper());

    @Test
    void renderEscapesUserTextAndInterpolatesProvidedTokens() {
        when(repository.findAssignedToDocumentType("FACTURE"))
                .thenReturn(List.of(PdfTemplate.builder().active(true).blocksJson("[{\"id\":\"b1\",\"kind\":\"text\",\"content\":\"{{client}}\"}]").build()));

        String html = service.renderIfConfigured("facture", Map.of("client", "<script>alert(1)</script>"));

        assertNotNull(html);
        assertTrue(html.contains("&lt;script&gt;alert(1)&lt;/script&gt;"));
        assertFalse(html.contains("<script>"));
    }

    @Test
    void rejectsImagesThatAreNotCloudinaryUrls() {
        PdfTemplateWriteRequest request = new PdfTemplateWriteRequest(
                "Modèle", "FACTURE", false,
                List.of(new PdfBlockRequest("img", "image", "https://example.com/image.svg", "left", "100%")));

        assertThrows(BadRequestException.class, () -> service.create(request, null));
        verifyNoInteractions(repository);
    }

    @Test
    void blankTemplateIsValidAndSerializedAsAnEmptyBlockList() {
        when(repository.findFirstByDocumentTypeAndNameOrderByVersionDesc("FACTURE", "Vierge"))
                .thenReturn(Optional.empty());
        when(repository.save(any(PdfTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(new PdfTemplateWriteRequest("Vierge", "facture", true, List.of()), null);

        assertEquals("FACTURE", response.documentType());
        assertTrue(response.active());
        assertTrue(response.blocks().isEmpty());
        verify(repository, never()).deactivateByDocumentType("FACTURE");
        verify(repository).save(argThat(t -> "AVEC_ENTETE".equals(t.getLayoutKey())));
    }

    @Test
    void activatingInvoiceTemplatesKeepsOtherInvoiceTemplatesAvailable() {
        PdfTemplate selected = PdfTemplate.builder().id(21L).name("Sans entête").documentType("FACTURE").layoutKey("SANS_ENTETE").version(1).active(false).blocksJson("[]").build();
        when(repository.findById(21L)).thenReturn(Optional.of(selected));
        when(repository.save(any(PdfTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var activated = service.activate(21L);

        assertTrue(activated.active());
        assertEquals("SANS_ENTETE", activated.layoutKey());
        verify(repository, never()).deactivateByDocumentType(anyString());
        verify(repository, never()).deactivateByDocumentTypeAndLayoutKey(anyString(), anyString());
        verify(repository).deactivateFamily("FACTURE", "Sans entête", "SANS_ENTETE");
    }

    @Test
    void acceptsCloudinaryImageUrl() {
        String url = "https://res.cloudinary.com/oas/image/upload/v1/oas/pdf-templates/logo.png";
        PdfTemplateWriteRequest request = new PdfTemplateWriteRequest("Avec logo", "FACTURE", false,
                List.of(new PdfBlockRequest("logo", "image", url, "center", "50%")));
        when(repository.findFirstByDocumentTypeAndNameOrderByVersionDesc("FACTURE", "Avec logo")).thenReturn(Optional.empty());
        when(repository.save(any(PdfTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals("AVEC_ENTETE", service.create(request, null).layoutKey());
    }

    @Test
    void allowsOneTemplateToBeAssignedToSeveralDocumentTypes() {
        PdfTemplateWriteRequest request = new PdfTemplateWriteRequest("Partagé", "FACTURE", false, List.of(), "AVEC_ENTETE",
                List.of("FACTURE", "PROFORMA", "DEVIS_PREVISIONNEL"));
        when(repository.findFirstByDocumentTypeAndNameOrderByVersionDesc("FACTURE", "Partagé")).thenReturn(Optional.empty());
        when(repository.save(any(PdfTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.create(request, null);

        assertEquals(Set.of("FACTURE", "PROFORMA", "DEVIS_PREVISIONNEL"), result.documentTypes());
        verify(repository).save(argThat(template -> template.getAssignedDocumentTypes().containsAll(request.documentTypes())));
    }

    @Test
    void revisingATemplateCanChangeItsAssignedDocumentTypesAndKeepsTheOldVersion() {
        PdfTemplate previous = PdfTemplate.builder().id(44L).name("Partagé").documentType("FACTURE").layoutKey("AVEC_ENTETE")
                .version(1).active(true).blocksJson("[]").assignedDocumentTypes(new java.util.LinkedHashSet<>(List.of("FACTURE"))).build();
        PdfTemplateWriteRequest revision = new PdfTemplateWriteRequest("Partagé", "PROFORMA", true, List.of(), "STANDARD",
                List.of("PROFORMA", "DIAGNOSTIC"));
        when(repository.findById(44L)).thenReturn(Optional.of(previous));
        when(repository.findFirstByDocumentTypeAndNameOrderByVersionDesc("PROFORMA", "Partagé")).thenReturn(Optional.empty());
        when(repository.save(any(PdfTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var saved = service.revise(44L, revision, null);

        assertEquals(2, saved.version());
        assertEquals(Set.of("PROFORMA", "DIAGNOSTIC"), saved.documentTypes());
        assertFalse(previous.isActive());
        assertEquals(Set.of("FACTURE"), previous.getAssignedDocumentTypes());
    }

    @Test
    void rendersConfigurableTableHeaderColumnsStylesAndTokens() {
        PdfBlockRequest table = new PdfBlockRequest("items", "table", "Article\tTotal\n{{designation}}\t12000", "left", "100%",
                null, "#112233", "#ffffff", "30%,70%", null, null, 6, true, null, "flow", null, null);

        String html = service.renderPreview(List.of(table), Map.of("designation", "Filtre à huile"));

        assertTrue(html.contains("<th"));
        assertTrue(html.contains("width:30%"));
        assertTrue(html.contains("Filtre à huile"));
        assertTrue(html.contains("#112233"));
    }

    @Test
    void rendersInvoiceLineItemsFromTheSelectedTableSource() {
        PdfBlockRequest table = new PdfBlockRequest("items", "table", "Référence\tDésignation\tQté\tTotal\n{{reference}}\t{{designation}}\t{{quantite}}\t{{montant}}", "left", "100%",
                null, null, null, "15%,35%,10%,40%", "grid", "middle", 5, true, "LIGNES_PIECES", "flow", null, null);
        Map<String, List<Map<String, String>>> rows = Map.of("LIGNES_PIECES", List.of(Map.of(
                "reference", "IG012025", "designation", "Huile moteur", "quantite", "1", "montant", "4 133")));

        String html = service.renderPreview(List.of(table), Map.of(), rows);

        assertTrue(html.contains("IG012025"));
        assertTrue(html.contains("Huile moteur"));
        assertTrue(html.contains("4 133"));
    }

    @Test
    void rejectsInvoiceLineSourcesForOtherDocumentTypes() {
        PdfBlockRequest table = new PdfBlockRequest("items", "table", "Désignation\n{{designation}}", "left", "100%",
                null, null, null, null, null, null, null, false, "LIGNES_PIECES", "flow", null, null);

        assertThrows(BadRequestException.class, () -> service.create(new PdfTemplateWriteRequest("Proforma", "PROFORMA", false, List.of(table)), null));
        verifyNoInteractions(repository);
    }

    @Test
    void rendersImageAtFreePageCoordinatesAndRejectsOutOfRangeCoordinates() {
        String url = "https://res.cloudinary.com/oas/image/upload/v1/oas/pdf-templates/logo.png";
        PdfBlockRequest positioned = new PdfBlockRequest("logo", "image", url, "left", "25%",
                null, null, null, null, null, null, null, false, null, "absolute", 22, 48);

        String html = service.renderPreview(List.of(positioned), Map.of());

        assertTrue(html.contains("left:22%;top:48%"));
        PdfBlockRequest outsidePage = new PdfBlockRequest("logo", "image", url, "left", "25%",
                null, null, null, null, null, null, null, false, null, "absolute", 101, 48);
        assertThrows(BadRequestException.class, () -> service.renderPreview(List.of(outsidePage), Map.of()));
    }

    @Test
    void deactivatingATemplateOnlyRemovesItFromNewAssignments() {
        PdfTemplate selected = PdfTemplate.builder().id(22L).name("OAS").documentType("FACTURE").layoutKey("AVEC_ENTETE").version(2).active(true).blocksJson("[]").build();
        when(repository.findById(22L)).thenReturn(Optional.of(selected));
        when(repository.save(any(PdfTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.deactivate(22L);

        assertFalse(result.active());
        assertEquals(22L, result.id());
    }

    @Test
    void savingAnInactiveInvoiceDraftDoesNotDisableTheActiveVersion() {
        PdfTemplate active = PdfTemplate.builder().id(30L).name("OAS").documentType("FACTURE").layoutKey("AVEC_ENTETE").version(1).active(true).blocksJson("[]").build();
        when(repository.findFirstByDocumentTypeAndNameOrderByVersionDesc("FACTURE", "OAS")).thenReturn(Optional.of(active));
        when(repository.save(any(PdfTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var draft = service.create(new PdfTemplateWriteRequest("OAS", "FACTURE", false, List.of()), null);

        assertFalse(draft.active());
        verify(repository, never()).deactivateFamily("FACTURE", "OAS", "AVEC_ENTETE");
    }
}
