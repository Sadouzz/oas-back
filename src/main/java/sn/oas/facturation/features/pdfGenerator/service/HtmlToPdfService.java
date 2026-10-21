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
        return genererHtmlEnPdf(htmlContent, documentType, tokens, "AVEC_ENTETE");
    }

    public byte[] genererHtmlEnPdf(String htmlContent, String documentType, Map<String, String> tokens, String layoutKey) {
        String templateHtml = templateService.renderIfConfigured(documentType, layoutKey, tokens);
        if (templateHtml != null) return genererHtmlEnPdf(templateHtml);
        String content = "SANS_ENTETE".equalsIgnoreCase(layoutKey) ? htmlContent : addBranding(htmlContent);
        return genererHtmlEnPdf(content);
    }

    public byte[] genererTemplatePdfSiConfigure(String documentType, Map<String, String> tokens) {
        String templateHtml = templateService.renderIfConfigured(documentType, "AVEC_ENTETE", tokens);
        return templateHtml == null ? null : genererHtmlEnPdf(templateHtml);
    }

    public byte[] genererTemplatePdfSiConfigure(String documentType, String layoutKey, Map<String, String> tokens) {
        String templateHtml = templateService.renderIfConfigured(documentType, layoutKey, tokens);
        return templateHtml == null ? null : genererHtmlEnPdf(templateHtml);
    }

    /** Ajoute une annexe métier obligatoire après le modèle configuré. */
    public byte[] genererTemplatePdfSiConfigure(String documentType, Map<String, String> tokens, String appendixHtml) {
        String templateHtml = templateService.renderIfConfigured(documentType, tokens);
        if (templateHtml == null) return null;
        String lower = templateHtml.toLowerCase(java.util.Locale.ROOT);
        int bodyEnd = lower.lastIndexOf("</body>");
        String html = bodyEnd >= 0
                ? templateHtml.substring(0, bodyEnd) + appendixHtml + templateHtml.substring(bodyEnd)
                : templateHtml + appendixHtml;
        return genererHtmlEnPdf(html);
    }

    public byte[] genererTemplatePdfSiConfigure(String documentType, String layoutKey, Map<String, String> tokens, String appendixHtml) {
        String templateHtml = templateService.renderIfConfigured(documentType, layoutKey, tokens);
        if (templateHtml == null) return null;
        String lower = templateHtml.toLowerCase(java.util.Locale.ROOT);
        int bodyEnd = lower.lastIndexOf("</body>");
        String html = bodyEnd >= 0
                ? templateHtml.substring(0, bodyEnd) + appendixHtml + templateHtml.substring(bodyEnd)
                : templateHtml + appendixHtml;
        return genererHtmlEnPdf(html);
    }

    public byte[] genererTemplatePdf(Long templateId, Map<String, String> tokens) {
        return genererTemplatePdf(templateId, tokens, Map.of());
    }

    public byte[] genererTemplatePdf(Long templateId, Map<String, String> tokens, Map<String, List<Map<String, String>>> tableData) {
        return genererHtmlEnPdf(templateService.renderById(templateId, tokens, tableData));
    }

    public byte[] genererApercuPdf(String html) { return genererHtmlEnPdf(html); }

    private String addBranding(String html) {
        String logo = brandingDataUri("oas-logo.png");
        String watermark = brandingDataUri("vehicle-brand-watermark.png");
        String css = "<style>@page{size:A4;margin:32mm 12mm 22mm}body{margin:0!important;font-family:Arial,sans-serif}.oas-brand-watermark{position:fixed;top:52mm;left:-12mm;width:210mm;max-width:none;z-index:-1}.oas-brand-header{position:fixed;top:-28mm;left:0;width:100%;height:25mm}.oas-brand-header table{width:100%;border:0;border-collapse:collapse}.oas-brand-header td{border:0;padding:0;vertical-align:middle}.oas-brand-logo{width:20mm}.oas-brand-logo img{width:18mm;height:18mm}.oas-brand-headline{font-size:8pt;color:#262d36;font-weight:bold;line-height:1.2}.oas-brand-services{font-size:6.5pt;color:#464b50;line-height:1.2}.oas-brand-footer{position:fixed;bottom:-19mm;left:0;width:100%;text-align:center;color:#373c42;font-size:7pt;line-height:1.25}.oas-brand-footer .brand-strip{margin-bottom:2mm}.oas-brand-footer .rule{border-top:.5pt solid #c3c7ca;margin-bottom:2mm}</style>";
        int headEnd = html.toLowerCase(java.util.Locale.ROOT).lastIndexOf("</head>");
        if (headEnd >= 0) html = html.substring(0, headEnd) + css + html.substring(headEnd);
        int bodyStart = html.toLowerCase(java.util.Locale.ROOT).indexOf("<body");
        if (bodyStart >= 0) {
            int bodyTagEnd = html.indexOf('>', bodyStart);
            if (bodyTagEnd >= 0) {
                String chrome = "<img class=\"oas-brand-watermark\" src=\"" + watermark + "\"/><div class=\"oas-brand-header\"><table><tbody><tr><td class=\"oas-brand-logo\"><img src=\"" + logo + "\"/></td><td><div class=\"oas-brand-headline\">SPÉCIALISTE VOITURES ALLEMANDES<br/>ET PROFESSIONNEL AUTOMOBILES TOUTES MARQUES</div><div class=\"oas-brand-services\">MÉCANIQUE • ÉLECTRICITÉ • VIDANGE • CLIMATISATION<br/>TÔLERIE • PEINTURE</div></td></tr></tbody></table></div><div class=\"oas-brand-footer\"><div class=\"rule\"></div><div class=\"brand-strip\">Mercedes-Benz　 Porsche　 BMW　 LAND ROVER　 BYD　 TESLA　 Audi　 Volkswagen</div><div>Km 2,5 Bd du Centenaire, Rond Point CYRNOS — BP 14092 Dakar, Sénégal</div><div>Tél. : +221 33 821 47 11 / +221 78 596 86 42 — orientautoservice@gmail.com</div></div>";
                html = html.substring(0, bodyTagEnd + 1) + chrome + html.substring(bodyTagEnd + 1);
            }
        }
        return html;
    }

    private String brandingDataUri(String assetName) {
        try (InputStream stream = getClass().getResourceAsStream("/branding/" + assetName)) {
            if (stream == null) return "";
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(stream.readAllBytes());
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Impossible de charger l’identité visuelle OAS pour le PDF.", e);
        }
    }
}
