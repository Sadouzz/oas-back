package sn.oas.facturation.features.pdfGenerator.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.IOException;

/** Shared OAS invoice-paper header, watermark and footer for branded PDFs. */
public final class OasBrandedPdfPageEvent extends PdfPageEventHelper {
    private final Image logo;
    private final Image marques;
    private final Font accroche = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(38, 45, 54));
    private final Font services = FontFactory.getFont(FontFactory.HELVETICA, 6.5f, new Color(70, 75, 80));
    private final Font pied = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(55, 60, 66));

    public OasBrandedPdfPageEvent() throws IOException, com.lowagie.text.BadElementException {
        logo = Image.getInstance(OasBrandedPdfPageEvent.class.getResource("/branding/oas-logo.png"));
        marques = Image.getInstance(OasBrandedPdfPageEvent.class.getResource("/branding/vehicle-brand-watermark.png"));
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        try {
            float pageWidth = document.getPageSize().getWidth();
            float pageHeight = document.getPageSize().getHeight();
            Image watermark = Image.getInstance(marques);
            watermark.scaleToFit(pageWidth, 402);
            watermark.setAbsolutePosition((pageWidth - watermark.getScaledWidth()) / 2, 200);
            writer.getDirectContentUnder().addImage(watermark);

            PdfContentByte canvas = writer.getDirectContent();
            Image logoCopy = Image.getInstance(logo);
            logoCopy.scaleToFit(51, 51);
            logoCopy.setAbsolutePosition(34, pageHeight - 77);
            canvas.addImage(logoCopy);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                    new Phrase("SPÉCIALISTE VOITURES ALLEMANDES", accroche), 98, pageHeight - 42, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                    new Phrase("ET PROFESSIONNEL AUTOMOBILES TOUTES MARQUES", accroche), 98, pageHeight - 54, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                    new Phrase("MÉCANIQUE • ÉLECTRICITÉ • VIDANGE • CLIMATISATION", services), 34, pageHeight - 84, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                    new Phrase("TÔLERIE • PEINTURE", services), 34, pageHeight - 93, 0);
            canvas.setColorStroke(new Color(195, 199, 202));
            canvas.setLineWidth(.5f);
            canvas.moveTo(34, 59);
            canvas.lineTo(pageWidth - 34, 59);
            canvas.stroke();
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                    new Phrase("Mercedes-Benz     Porsche     BMW     LAND ROVER     BYD     TESLA     Audi     Volkswagen", pied),
                    pageWidth / 2, 46, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                    new Phrase("Km 2,5 Bd du Centenaire, Rond Point CYRNOS — BP 14092 Dakar, Sénégal", pied),
                    pageWidth / 2, 34, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                    new Phrase("Tél. : +221 33 821 47 11 / +221 78 596 86 42 — orientautoservice@gmail.com", pied),
                    pageWidth / 2, 23, 0);
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'appliquer le papier à en-tête OAS au PDF", e);
        }
    }
}
