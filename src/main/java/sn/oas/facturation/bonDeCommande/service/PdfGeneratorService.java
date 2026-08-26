package sn.oas.facturation.bonDeCommande.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import sn.oas.facturation.bonDeCommande.data.entity.BonDeCommande;
import sn.oas.facturation.bonDeCommande.data.entity.LigneBonDeCommandePiece;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    public byte[] genererBonDeCommandePdf(BonDeCommande bonDeCommande) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Titre
            Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLACK);
            Paragraph titre = new Paragraph("BON DE COMMANDE", fontTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            titre.setSpacingAfter(20);
            document.add(titre);

            // Informations du Bon
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            
            document.add(new Paragraph("Numéro : " + bonDeCommande.getNumero(), fontBold));
            document.add(new Paragraph("Date : " + bonDeCommande.getDateCommande().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fontNormal));
            document.add(new Paragraph("Statut : " + bonDeCommande.getStatut().name(), fontNormal));
            
            if (bonDeCommande.getFournisseur() != null) {
                document.add(new Paragraph("\nFournisseur : " + bonDeCommande.getFournisseur().getNomEntreprise(), fontBold));
                document.add(new Paragraph("Contact : " + bonDeCommande.getFournisseur().getPrenom() + " " + bonDeCommande.getFournisseur().getNom(), fontNormal));
            }
            
            if (bonDeCommande.getVehicule() != null) {
                document.add(new Paragraph("Véhicule : " + bonDeCommande.getVehicule().getImmatriculation() + " (" + bonDeCommande.getVehicule().getMarque() + ")", fontNormal));
            }

            document.add(new Paragraph(" "));

            // Tableau des articles
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 2f, 1f, 2f, 2f});

            // En-têtes du tableau
            String[] headers = {"Désignation", "Référence", "Qte", "Prix Unitaire", "Montant"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, fontBold));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Lignes
            for (LigneBonDeCommandePiece ligne : bonDeCommande.getLignes()) {
                String designation = ligne.getPieceDetachee() != null ? ligne.getPieceDetachee().getCategorie() : ligne.getDesignationPds();
                String reference = ligne.getPieceDetachee() != null ? ligne.getPieceDetachee().getDesignation() : ligne.getReferencePds();
                
                table.addCell(new Phrase(designation != null ? designation : "", fontNormal));
                table.addCell(new Phrase(reference != null ? reference : "", fontNormal));
                
                PdfPCell qteCell = new PdfPCell(new Phrase(String.valueOf(ligne.getQuantite()), fontNormal));
                qteCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(qteCell);
                
                PdfPCell puCell = new PdfPCell(new Phrase(ligne.getPrixUnitaire().toString() + " FCFA", fontNormal));
                puCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(puCell);
                
                PdfPCell montantCell = new PdfPCell(new Phrase(ligne.getMontant().toString() + " FCFA", fontNormal));
                montantCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(montantCell);
            }
            
            document.add(table);
            document.add(new Paragraph(" "));

            // Totaux
            Paragraph totals = new Paragraph();
            totals.setAlignment(Element.ALIGN_RIGHT);
            
            if (bonDeCommande.getTvaApplicable() != null && bonDeCommande.getTvaApplicable()) {
                totals.add(new Phrase("Montant HT : " + bonDeCommande.getMontantHT() + " FCFA\n", fontNormal));
                totals.add(new Phrase("TVA (18%) : " + bonDeCommande.getMontantTVA() + " FCFA\n", fontNormal));
                totals.add(new Phrase("Montant TTC : " + bonDeCommande.getMontantTTC() + " FCFA", fontBold));
            } else {
                totals.add(new Phrase("Total : " + bonDeCommande.getMontantHT() + " FCFA", fontBold));
            }
            
            document.add(totals);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }

        return out.toByteArray();
    }
}
