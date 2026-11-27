package sn.oas.facturation.features.recu.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import sn.oas.facturation.features.pdfGenerator.service.OasBrandedPdfPageEvent;
import sn.oas.facturation.features.recu.data.enums.TypeDocumentRecu;
import sn.oas.facturation.features.recu.dto.DocumentRecuDto;
import sn.oas.facturation.features.recu.dto.RecuDetailResponse;
import sn.oas.facturation.features.recu.dto.VersementClientDto;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/** Mise en page du reçu client (équivalent de settingpdf_recu.php de l'ancienne application). */
final class RecuPdf {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color ENTETE_TABLEAU = new Color(99, 175, 222);

    private static final Font TITRE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font SOUS_TITRE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font TEXTE = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font TEXTE_GRAS = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font ENTETE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font CELLULE = FontFactory.getFont(FontFactory.HELVETICA, 9);

    private RecuPdf() {
    }

    static byte[] generer(RecuDetailResponse recu, boolean avecEntete) {
        Document document = avecEntete
                ? new Document(PageSize.A4, 34, 34, 105, 78)
                : new Document(PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            if (avecEntete) {
                writer.setPageEvent(new OasBrandedPdfPageEvent());
            }
            document.open();

            Paragraph titre = new Paragraph("REÇU N° " + recu.numeroRecu(), TITRE);
            titre.setAlignment(Element.ALIGN_CENTER);
            titre.setSpacingAfter(14);
            document.add(titre);

            document.add(new Paragraph("Client : " + texte(recu.nomClient())
                    + (recu.numeroClient() == null ? "" : " (n° " + recu.numeroClient() + ")"), TEXTE_GRAS));
            if (recu.telephone() != null && !recu.telephone().isBlank()) {
                document.add(new Paragraph("Téléphone : " + recu.telephone(), TEXTE));
            }
            document.add(new Paragraph("Date de début : " + date(recu.dateDebut(), DATE_HEURE), TEXTE));
            if (recu.dateFin() != null) {
                document.add(new Paragraph("Date de fin : " + date(recu.dateFin(), DATE_HEURE), TEXTE));
            }
            document.add(new Paragraph("Statut : " + statut(recu), TEXTE));

            documents(document, "Factures", recu.factures(), true, recu.totalFactures());
            documents(document, "Notes de prix", recu.notesDePrix(), false, recu.totalNotesDePrix());
            versements(document, recu.versements());

            PdfPTable totaux = new PdfPTable(2);
            totaux.setWidthPercentage(55);
            totaux.setWidths(new float[]{3f, 2f});
            totaux.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totaux.setSpacingBefore(14);
            ligneTotal(totaux, "Total (factures + notes de prix)", recu.montant());
            ligneTotal(totaux, "Total payé", recu.montantPaye());
            ligneTotal(totaux, "Reste à payer", recu.resteAPayer());
            document.add(totaux);

            document.close();
        } catch (DocumentException | java.io.IOException e) {
            throw new IllegalStateException("Erreur lors de la génération du PDF du reçu " + recu.numeroRecu(), e);
        }
        return out.toByteArray();
    }

    private static void documents(Document document, String titre, List<DocumentRecuDto> docs, boolean factures,
                                  BigDecimal total) throws DocumentException {
        if (docs == null || docs.isEmpty()) {
            return;
        }
        sousTitre(document, titre);
        String[] colonnes = factures
                ? new String[]{"Date", "Numéro", "Total HT", "Total TTC", "Payé", "Reste"}
                : new String[]{"Date", "Numéro", "Montant", "Payé", "Reste"};
        PdfPTable table = tableau(colonnes, factures ? new float[]{2f, 3f, 2f, 2f, 2f, 2f} : new float[]{2f, 3f, 2f, 2f, 2f});
        for (DocumentRecuDto d : docs) {
            cellule(table, date(d.date(), DATE), Element.ALIGN_CENTER);
            cellule(table, texte(d.numero()), Element.ALIGN_LEFT);
            if (factures) {
                cellule(table, montant(d.montantHT()), Element.ALIGN_RIGHT);
            }
            cellule(table, montant(d.montantTotal()), Element.ALIGN_RIGHT);
            cellule(table, montant(d.montantPaye()), Element.ALIGN_RIGHT);
            cellule(table, montant(d.resteAPayer()), Element.ALIGN_RIGHT);
        }
        document.add(table);
        Paragraph p = new Paragraph("Total " + titre.toLowerCase() + " : " + montant(total), TEXTE_GRAS);
        p.setAlignment(Element.ALIGN_RIGHT);
        document.add(p);
    }

    private static void versements(Document document, List<VersementClientDto> versements) throws DocumentException {
        if (versements == null || versements.isEmpty()) {
            return;
        }
        sousTitre(document, "Versements");
        PdfPTable table = tableau(new String[]{"Date", "Montant", "Mode", "Réparti sur"}, new float[]{2.2f, 2f, 1.8f, 5f});
        for (VersementClientDto v : versements) {
            cellule(table, date(v.dateVersement(), DATE_HEURE), Element.ALIGN_CENTER);
            cellule(table, montant(v.montant()), Element.ALIGN_RIGHT);
            cellule(table, texte(v.modePaiement()), Element.ALIGN_CENTER);
            String imputations = v.imputations() == null ? "" : v.imputations().stream()
                    .map(i -> (i.type() == TypeDocumentRecu.FACTURE ? "Facture " : "Note de prix ")
                            + texte(i.numeroDocument())
                            + (i.numeroRecuFacture() == null ? "" : " (reçu " + i.numeroRecuFacture() + ")")
                            + " : " + montant(i.montant()))
                    .collect(Collectors.joining("\n"));
            cellule(table, imputations, Element.ALIGN_LEFT);
        }
        document.add(table);
    }

    private static void sousTitre(Document document, String titre) throws DocumentException {
        Paragraph p = new Paragraph(titre, SOUS_TITRE);
        p.setSpacingBefore(14);
        p.setSpacingAfter(6);
        document.add(p);
    }

    private static PdfPTable tableau(String[] colonnes, float[] largeurs) throws DocumentException {
        PdfPTable table = new PdfPTable(colonnes.length);
        table.setWidthPercentage(100);
        table.setWidths(largeurs);
        table.setHeaderRows(1);
        for (String colonne : colonnes) {
            PdfPCell cell = new PdfPCell(new Phrase(colonne, ENTETE));
            cell.setBackgroundColor(ENTETE_TABLEAU);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            table.addCell(cell);
        }
        return table;
    }

    private static void cellule(PdfPTable table, String valeur, int alignement) {
        PdfPCell cell = new PdfPCell(new Phrase(valeur, CELLULE));
        cell.setHorizontalAlignment(alignement);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private static void ligneTotal(PdfPTable table, String libelle, BigDecimal valeur) {
        PdfPCell l = new PdfPCell(new Phrase(libelle, TEXTE));
        l.setPadding(4);
        table.addCell(l);
        PdfPCell v = new PdfPCell(new Phrase(montant(valeur), TEXTE_GRAS));
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        v.setPadding(4);
        table.addCell(v);
    }

    private static String statut(RecuDetailResponse recu) {
        if (recu.enCours()) {
            return "En cours";
        }
        return recu.valide() ? "Payé et validé le " + date(recu.dateValidation(), DATE) : "Payé";
    }

    /** Montant au format de l'ancienne application : 1.250.000 F CFA. */
    static String montant(BigDecimal valeur) {
        DecimalFormatSymbols symboles = new DecimalFormatSymbols();
        symboles.setGroupingSeparator('.');
        symboles.setDecimalSeparator(',');
        DecimalFormat format = new DecimalFormat("#,##0.##", symboles);
        BigDecimal v = valeur == null ? BigDecimal.ZERO : valeur.setScale(2, RoundingMode.HALF_UP);
        return format.format(v) + " F CFA";
    }

    private static String date(LocalDateTime valeur, DateTimeFormatter format) {
        return valeur == null ? "" : valeur.format(format);
    }

    private static String texte(String valeur) {
        return valeur == null ? "" : valeur;
    }
}
