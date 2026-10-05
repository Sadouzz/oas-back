package sn.oas.facturation.features.pdfGenerator.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import sn.oas.facturation.features.pdfTemplate.service.PdfTemplateService;
import java.util.Map;
import java.util.List;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

/**
 * Convertit du contenu HTML/CSS en PDF via Flying Saucer + OpenPDF.
 * Comportement identique à html2pdf (PHP).
 */
@Service
@RequiredArgsConstructor
public class HtmlToPdfService {
    private final PdfTemplateService templateService;

    /**
     * Génère un PDF à partir d'un contenu HTML (XHTML valide).
     *
     * @param htmlContent le contenu HTML (doit être du XHTML bien formé)
     * @return les bytes du fichier PDF généré
     */
    public byte[] genererHtmlEnPdf(String htmlContent) {
        try {
            ITextRenderer renderer = new ITextRenderer();
            renderer.getSharedContext().setUserAgentCallback(new ITextUserAgent(renderer.getOutputDevice(), ITextRenderer.DEFAULT_DOTS_PER_PIXEL) {
                @Override
                protected InputStream resolveAndOpenStream(String uri) {
                    if (uri != null && uri.matches("^data:image/(png|jpeg);base64,[A-Za-z0-9+/=]+$")) {
                        return new ByteArrayInputStream(Base64.getDecoder().decode(uri.substring(uri.indexOf(',') + 1)));
                    }
                    return super.resolveAndOpenStream(uri);
                }
            });
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            renderer.createPDF(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF via Flying Saucer", e);
        }
    }

    public byte[] genererHtmlEnPdf(String htmlContent, String documentType, Map<String, String> tokens) {
        String templateHtml = templateService.renderIfConfigured(documentType, tokens);
        return genererHtmlEnPdf(templateHtml != null ? templateHtml : htmlContent);
    }

    public byte[] genererTemplatePdfSiConfigure(String documentType, Map<String, String> tokens) {
        String templateHtml = templateService.renderIfConfigured(documentType, tokens);
        return templateHtml == null ? null : genererHtmlEnPdf(templateHtml);
    }

    public byte[] genererTemplatePdf(Long templateId, Map<String, String> tokens) {
        return genererTemplatePdf(templateId, tokens, Map.of());
    }

    public byte[] genererTemplatePdf(Long templateId, Map<String, String> tokens, Map<String, List<Map<String, String>>> tableData) {
        return genererHtmlEnPdf(templateService.renderById(templateId, tokens, tableData));
    }

    public byte[] genererApercuPdf(String html) { return genererHtmlEnPdf(html); }
}
