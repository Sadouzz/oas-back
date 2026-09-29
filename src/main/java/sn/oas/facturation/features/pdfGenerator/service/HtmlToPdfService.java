package sn.oas.facturation.features.pdfGenerator.service;

import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;

/**
 * Convertit du contenu HTML/CSS en PDF via Flying Saucer + OpenPDF.
 * Comportement identique à html2pdf (PHP).
 */
@Service
public class HtmlToPdfService {

    /**
     * Génère un PDF à partir d'un contenu HTML (XHTML valide).
     *
     * @param htmlContent le contenu HTML (doit être du XHTML bien formé)
     * @return les bytes du fichier PDF généré
     */
    public byte[] genererHtmlEnPdf(String htmlContent) {
        try {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            renderer.createPDF(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF via Flying Saucer", e);
        }
    }
}