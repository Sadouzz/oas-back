package sn.oas.facturation.features.bonDeReception.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.user.repository.UserRepository;
import sn.oas.facturation.features.bonDeCommande.repository.BonDeCommandeRepository;
import sn.oas.facturation.features.bonDeReception.data.entity.BonDeReception;
import sn.oas.facturation.features.bonDeReception.dto.BonDeReceptionCreateRequest;
import sn.oas.facturation.features.bonDeReception.dto.BonDeReceptionResponse;
import sn.oas.facturation.features.bonDeReception.dto.BonDeReceptionUpdateRequest;
import sn.oas.facturation.features.bonDeReception.repository.BonDeReceptionRepository;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationMainDoeuvre;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import sn.oas.facturation.features.pdfGenerator.service.OasBrandedPdfPageEvent;
import sn.oas.facturation.features.pdfGenerator.service.PdfLayout;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

import sn.oas.facturation.features.main_doeuvre.repository.MainDoeuvreRepository;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BonDeReceptionServiceImpl implements BonDeReceptionService {

    private final BonDeReceptionRepository bonDeReceptionRepository;
    private final BonDeCommandeRepository bonDeCommandeRepository;
    private final UserRepository userRepository;

    private final PieceDetacheRepository pieceDetacheRepository;
    private final MainDoeuvreRepository mainDoeuvreRepository;
    private final sn.oas.facturation.features.pdfGenerator.service.HtmlToPdfService htmlToPdfService;

    @Override
    @Transactional
    public BonDeReception create(BonDeReceptionCreateRequest request) {
        throw new UnsupportedOperationException(
                "Les bons de réception sont générés automatiquement lors de la réception d'un bon de commande.");
    }

    @Override
    @Transactional
    public BonDeReception update(Long id, BonDeReceptionUpdateRequest request) {
        throw new UnsupportedOperationException("Un bon de réception ne peut pas être modifié.");
    }

    @Override
    @Transactional(readOnly = true)
    public BonDeReception getById(Long id) {
        return bonDeReceptionRepository.findById(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Bon de réception non trouvé avec l'id : " + id));
    }

    @Override
    public Page<BonDeReception> getAllBonsDeReception(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return bonDeReceptionRepository.findAll(pageable);
    }

    @Override
    public Page<BonDeReception> getAll(int page, int size) {
        return getAllBonsDeReception(page, size);
    }

    @Override
    public List<BonDeReception> getAll() {
        return bonDeReceptionRepository.findAll();
    }

    @Override
    public List<BonDeReception> searchBonsDeReception(String keyword) {
        return bonDeReceptionRepository.searchBonsDeReception(keyword);
    }

    @Override
    public List<BonDeReception> search(String keyword) {
        return searchBonsDeReception(keyword);
    }

    @Override
    public Page<BonDeReception> searchBonsDeReception(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return bonDeReceptionRepository.searchBonsDeReception(keyword, pageable);
    }

    @Override
    public Page<BonDeReception> search(String keyword, int page, int size) {
        return searchBonsDeReception(keyword, page, size);
    }

    @Override
    public List<BonDeReception> getRecentBonsDeReception() {
        return bonDeReceptionRepository.findTop5ByOrderByDateCreationDesc();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!bonDeReceptionRepository.existsById(id)) {
            throw new sn.oas.facturation.shared.exception.ResourceNotFoundException("Bon de réception non trouvé avec l'id : " + id);
        }
        bonDeReceptionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id) {
        return generatePdf(id, "AVEC_ENTETE");
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id, String layoutKey) {
        String layout = PdfLayout.normalize(layoutKey);
        boolean withHeader = PdfLayout.AVEC_ENTETE.equals(layout);
        BonDeReception bl = bonDeReceptionRepository.findById(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Bon de réception non trouvé avec l'id : " + id));

        var response = BonDeReceptionResponse.from(bl);
        byte[] configured = htmlToPdfService.genererTemplatePdfSiConfigure("BON_RECEPTION", layout, java.util.Map.ofEntries(
                java.util.Map.entry("numero", templateValue(bl.getNumero())),
                java.util.Map.entry("date", templateValue(bl.getDateCreation())),
                java.util.Map.entry("statut", htmlEscapeStatus(response.getStatut())),
                java.util.Map.entry("fournisseur", templateValue(response.getFournisseurNom())),
                java.util.Map.entry("referenceFactureFournisseur", templateValue(bl.getReferenceFactureFournisseur())),
                java.util.Map.entry("bonDeCommande", templateValue(response.getBonDeCommandeNumero())),
                java.util.Map.entry("agent", templateValue(response.getAgentNom())),
                java.util.Map.entry("remarque", templateValue(bl.getRemarque())),
                java.util.Map.entry("montantHT", templateValue(bl.getMontantHT())),
                java.util.Map.entry("montantTVA", templateValue(bl.getMontantTVA())),
                java.util.Map.entry("montantTimbre", templateValue(bl.getMontantTimbre())),
                java.util.Map.entry("montantTTC", templateValue(bl.getMontantTTC())),
                java.util.Map.entry("montantTotal", templateValue(bl.getMontantTotal()))),
                buildReceptionAppendix(bl, response, withHeader));
        if (configured != null) return configured;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(com.lowagie.text.PageSize.A4, 34, 34, withHeader ? 105 : 34, withHeader ? 78 : 34);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            if (withHeader) writer.setPageEvent(new OasBrandedPdfPageEvent());
            document.open();

            // Polices
            Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(24, 43, 68));
            Font fontSousTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fontTexte = FontFactory.getFont(FontFactory.HELVETICA, 8.5f);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, Color.WHITE);

            // Titre du document
            Paragraph titre = new Paragraph("BON DE RÉCEPTION", fontTitre);
            titre.setAlignment(Element.ALIGN_LEFT);
            titre.setSpacingAfter(10);
            document.add(titre);

            // Infos générales
            document.add(new Paragraph("N° : " + bl.getNumero(), fontSousTitre));
            document.add(new Paragraph("Date de réception : " + templateValue(bl.getDateCreation()), fontTexte));
            document.add(new Paragraph("Statut de la commande : " + htmlEscapeStatus(response.getStatut()), fontTexte));
            document.add(new Paragraph("Fournisseur : " + templateValue(response.getFournisseurNom()), fontTexte));
            document.add(new Paragraph("Facture fournisseur : " + templateValue(bl.getReferenceFactureFournisseur()), fontTexte));
            if (bl.getAgent() != null) {
                document.add(new Paragraph(
                        "Agent : " + bl.getAgent().getFirstName() + " " + bl.getAgent().getLastName(), fontTexte));
            }

            if (bl.getBonDeCommande() != null) {
                document.add(new Paragraph("Réf. Commande : " + bl.getBonDeCommande().getNumero(), fontTexte));
                if (bl.getBonDeCommande().getVehicule() != null) {
                    var vehicule = bl.getBonDeCommande().getVehicule();
                    document.add(new Paragraph("Véhicule : " + templateValue(vehicule.getImmatriculation()) + " — "
                            + templateValue(vehicule.getMarque()) + " " + templateValue(vehicule.getModele()), fontTexte));
                    if (vehicule.getClient() != null) {
                        document.add(new Paragraph("Client : " + templateValue(vehicule.getClient().getFirstName()) + " "
                                + templateValue(vehicule.getClient().getLastName()) + " — "
                                + templateValue(vehicule.getClient().getPhone()), fontTexte));
                    }
                }
            }
            document.add(new Paragraph("Kilométrage : " + bl.getKilometrage(), fontTexte));
            document.add(new Paragraph("Remarque : " + (bl.getRemarque() != null ? bl.getRemarque() : ""), fontTexte));

            document.add(new Paragraph(" "));

            // Tableau Pièces
            if (response.getLignesPieces() != null && !response.getLignesPieces().isEmpty()) {
                document.add(new Paragraph("Pièces :", fontSousTitre));
                document.add(new Paragraph(" "));

                PdfPTable tablePieces = new PdfPTable(7);
                tablePieces.setWidthPercentage(100);
                tablePieces.setWidths(new float[] { 3.5f, 1.05f, 1.05f, 1.15f, 1.0f, 1.45f, 1.45f });
                tablePieces.setSplitLate(false);

                for (String label : List.of("Pièce / réf. / dépôt", "Qté BR", "Commandée", "Reçue", "Reste", "Prix unit.", "Montant")) {
                    PdfPCell header = new PdfPCell(new Phrase(label, fontHeader));
                    header.setBackgroundColor(Color.DARK_GRAY);
                    tablePieces.addCell(header);
                }
                for (var ligne : response.getLignesPieces()) {
                    String designation = templateValue(ligne.getDesignationPiece())
                            + (ligne.getReferencePiece() == null ? "" : "\nRéf. " + ligne.getReferencePiece())
                            + (ligne.getDepotNom() == null ? "" : "\nDépôt : " + ligne.getDepotNom());
                    tablePieces.addCell(new Phrase(designation, fontTexte));
                    tablePieces.addCell(new Phrase(templateValue(ligne.getQuantite()), fontTexte));
                    tablePieces.addCell(new Phrase(templateValue(ligne.getQuantiteCommandee()), fontTexte));
                    tablePieces.addCell(new Phrase(templateValue(ligne.getQuantiteRecueTotale()), fontTexte));
                    tablePieces.addCell(new Phrase(templateValue(ligne.getQuantiteRestante()), fontTexte));
                    tablePieces.addCell(new Phrase(templateValue(ligne.getPrix()) + " FCFA", fontTexte));
                    tablePieces.addCell(new Phrase(templateValue(ligne.getMontantTotal()) + " FCFA", fontTexte));
                }

                document.add(tablePieces);
                document.add(new Paragraph(" "));
            }

            // Tableau Main d'œuvre
            if (bl.getLignesFacturationMainDoeuvres() != null && !bl.getLignesFacturationMainDoeuvres().isEmpty()) {
                document.add(new Paragraph("Main d'œuvre :", fontSousTitre));
                document.add(new Paragraph(" "));

                PdfPTable tableMo = new PdfPTable(4);
                tableMo.setWidthPercentage(100);
                tableMo.setWidths(new float[] { 4, 2, 2, 2 });

                PdfPCell cell1 = new PdfPCell(new Phrase("Description", fontHeader));
                cell1.setBackgroundColor(Color.DARK_GRAY);
                PdfPCell cell2 = new PdfPCell(new Phrase("Heures", fontHeader));
                cell2.setBackgroundColor(Color.DARK_GRAY);
                PdfPCell cell3 = new PdfPCell(new Phrase("Taux Horaire", fontHeader));
                cell3.setBackgroundColor(Color.DARK_GRAY);
                PdfPCell cell4 = new PdfPCell(new Phrase("Total", fontHeader));
                cell4.setBackgroundColor(Color.DARK_GRAY);

                tableMo.addCell(cell1);
                tableMo.addCell(cell2);
                tableMo.addCell(cell3);
                tableMo.addCell(cell4);

                for (LigneFacturationMainDoeuvre lm : bl.getLignesFacturationMainDoeuvres()) {
                    String description = lm.getMainDoeuvre() != null && lm.getMainDoeuvre().getCategorie() != null
                            ? lm.getMainDoeuvre().getCategorie().getNom()
                            : "-";
                    tableMo.addCell(new Phrase(description, fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(lm.getNbreHeure()), fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(lm.getTarifHoraire()), fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(lm.getNbreHeure() * lm.getTarifHoraire()), fontTexte));
                }

                document.add(tableMo);
                document.add(new Paragraph(" "));
            }

            // Totaux
            Paragraph total = new Paragraph(
                    "Total HT : " + templateValue(bl.getMontantHT()) + " FCFA\n" +
                            "TVA : " + templateValue(bl.getMontantTVA()) + " FCFA\n" +
                            "Timbre : " + templateValue(bl.getMontantTimbre()) + " FCFA\n" +
                            "Total TTC : " + templateValue(bl.getMontantTTC()) + " FCFA\n" +
                            "TOTAL GÉNÉRAL : " + templateValue(bl.getMontantTotal()) + " FCFA",
                    fontSousTitre);
            total.setSpacingBefore(10);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF", e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    private String templateValue(Object value) { return value == null ? "" : value.toString(); }

    private String buildReceptionAppendix(BonDeReception bon, BonDeReceptionResponse response, boolean withHeader) {
        StringBuilder html = new StringBuilder("<div style='page-break-before:always;font-family:Arial,sans-serif;font-size:9pt;color:#172b4d;position:relative'>");
        if (withHeader) html.append("<img src='").append(brandWatermarkDataUri()).append("' style='position:absolute;top:75px;left:-5.5%;width:111%;height:auto;z-index:-1'/>")
                .append("<table style='width:100%;border-bottom:1px solid #c3c7ca;margin-bottom:12px'><tr><td style='width:62px'><img src='").append(logoDataUri()).append("' style='width:49px;height:49px'/></td><td style='font-size:8pt;color:#262d36'><strong>SPÉCIALISTE VOITURES ALLEMANDES<br/>ET PROFESSIONNEL AUTOMOBILES TOUTES MARQUES</strong><br/><small>MÉCANIQUE • ÉLECTRICITÉ • VIDANGE • CLIMATISATION<br/>TÔLERIE • PEINTURE</small></td></tr></table>");
        html.append("<h1>Détail du bon de réception ").append(htmlEscape(templateValue(bon.getNumero()))).append("</h1>")
                .append("<h1>Détail du bon de réception ").append(htmlEscape(templateValue(bon.getNumero()))).append("</h1>")
                .append("<table style='width:100%;border-collapse:collapse;margin-bottom:14px'>")
                .append(htmlRow("Date de réception", templateValue(bon.getDateCreation())))
                .append(htmlRow("État de la commande", htmlEscapeStatus(response.getStatut())))
                .append(htmlRow("Fournisseur", response.getFournisseurNom()))
                .append(htmlRow("Référence facture fournisseur", bon.getReferenceFactureFournisseur()))
                .append(htmlRow("Bon de commande", response.getBonDeCommandeNumero()))
                .append(htmlRow("Agent", response.getAgentNom()))
                .append(htmlRow("Remarque", bon.getRemarque()));
        if (bon.getBonDeCommande() != null && bon.getBonDeCommande().getVehicule() != null) {
            var vehicule = bon.getBonDeCommande().getVehicule();
            html.append(htmlRow("Véhicule", templateValue(vehicule.getImmatriculation()) + " — "
                    + templateValue(vehicule.getMarque()) + " " + templateValue(vehicule.getModele())));
            if (vehicule.getClient() != null) html.append(htmlRow("Client", templateValue(vehicule.getClient().getFirstName())
                    + " " + templateValue(vehicule.getClient().getLastName()) + " — " + templateValue(vehicule.getClient().getPhone())));
        }
        html.append("</table><h2>Pièces et suivi des quantités</h2>")
                .append("<table style='width:100%;border-collapse:collapse'><thead><tr>");
        for (String heading : List.of("Pièce / référence / dépôt", "Qté sur ce BR", "Commandée", "Reçue cumulée", "Reste", "Prix unitaire", "Montant")) {
            html.append("<th style='background:#243b53;color:white;border:1px solid #ccd6e0;padding:6px'>")
                    .append(htmlEscape(heading)).append("</th>");
        }
        html.append("</tr></thead><tbody>");
        for (var ligne : response.getLignesPieces()) {
            String designation = templateValue(ligne.getDesignationPiece())
                    + (ligne.getReferencePiece() == null ? "" : "\nRéf. " + ligne.getReferencePiece())
                    + (ligne.getDepotNom() == null ? "" : "\nDépôt : " + ligne.getDepotNom());
            html.append("<tr>").append(htmlCell(designation)).append(htmlCell(templateValue(ligne.getQuantite())))
                    .append(htmlCell(templateValue(ligne.getQuantiteCommandee())))
                    .append(htmlCell(templateValue(ligne.getQuantiteRecueTotale())))
                    .append(htmlCell(templateValue(ligne.getQuantiteRestante())))
                    .append(htmlCell(templateValue(ligne.getPrix()) + " FCFA"))
                    .append(htmlCell(templateValue(ligne.getMontantTotal()) + " FCFA")).append("</tr>");
        }
        html.append("</tbody></table>");
        if (response.getLignesMainDoeuvres() != null && !response.getLignesMainDoeuvres().isEmpty()) {
            html.append("<h2>Main d'œuvre</h2><table style='width:100%;border-collapse:collapse'><thead><tr>");
            for (String heading : List.of("Description", "Heures", "Tarif horaire", "Total")) {
                html.append("<th style='background:#243b53;color:white;border:1px solid #ccd6e0;padding:6px'>")
                        .append(htmlEscape(heading)).append("</th>");
            }
            html.append("</tr></thead><tbody>");
            for (var ligne : response.getLignesMainDoeuvres()) html.append("<tr>")
                    .append(htmlCell(templateValue(ligne.getDescriptionMainDoeuvre())))
                    .append(htmlCell(templateValue(ligne.getNbreHeure())))
                    .append(htmlCell(templateValue(ligne.getTarifHoraire()) + " FCFA"))
                    .append(htmlCell(templateValue(ligne.getMontantTotal()) + " FCFA")).append("</tr>");
            html.append("</tbody></table>");
        }
        html.append("<h2>Totaux</h2><table style='width:45%;margin-left:auto;border-collapse:collapse'>")
                .append(htmlRow("Montant HT", templateValue(bon.getMontantHT()) + " FCFA"))
                .append(htmlRow("TVA", templateValue(bon.getMontantTVA()) + " FCFA"))
                .append(htmlRow("Timbre", templateValue(bon.getMontantTimbre()) + " FCFA"))
                .append(htmlRow("Total TTC", templateValue(bon.getMontantTTC()) + " FCFA"))
                .append(htmlRow("Net à payer", templateValue(bon.getMontantTotal()) + " FCFA"))
                .append("</table>");
        if (withHeader) html.append("<p style='text-align:center;border-top:1px solid #ccd6e0;padding-top:6px;margin-top:12px;font-size:7pt'>Mercedes-Benz　 Porsche　 BMW　 LAND ROVER　 BYD　 TESLA　 Audi　 Volkswagen<br/>Km 2,5 Bd du Centenaire, Rond Point CYRNOS — BP 14092 Dakar, Sénégal<br/>Tél. : +221 33 821 47 11 / +221 78 596 86 42 — orientautoservice@gmail.com</p>");
        html.append("</div>");
        return html.toString();
    }

    private String htmlRow(String label, Object value) {
        return "<tr><th style='text-align:left;border:1px solid #ccd6e0;padding:6px'>" + htmlEscape(label)
                + "</th><td style='border:1px solid #ccd6e0;padding:6px'>" + htmlEscape(templateValue(value)) + "</td></tr>";
    }

    private String htmlCell(String value) {
        return "<td style='border:1px solid #ccd6e0;padding:6px;white-space:pre-line'>" + htmlEscape(value) + "</td>";
    }

    private String htmlEscape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;").replace("\n", "<br/>");
    }

    private String htmlEscapeStatus(String statut) {
        return switch (statut == null ? "" : statut) {
            case "RECU" -> "Reçu";
            case "INCOMPLET" -> "Partiel";
            case "ENVOYE" -> "Envoyé";
            case "EN_ATTENTE" -> "En attente";
            case "ANNULE" -> "Annulé";
            default -> templateValue(statut);
        };
    }

    private String logoDataUri() {
        try (var stream = getClass().getResourceAsStream("/branding/oas-logo.png")) {
            if (stream == null) return "";
            return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(stream.readAllBytes());
        } catch (java.io.IOException e) {
            log.warn("Impossible de charger le logo OAS pour l'annexe PDF", e);
            return "";
        }
    }

    private String brandWatermarkDataUri() {
        try (var stream = getClass().getResourceAsStream("/branding/vehicle-brand-watermark.png")) {
            if (stream == null) return "";
            return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(stream.readAllBytes());
        } catch (java.io.IOException e) {
            log.warn("Impossible de charger les logos de marques pour le PDF", e);
            return "";
        }
    }
}
