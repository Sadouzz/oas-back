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
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationPiece;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreResponse;
import sn.oas.facturation.features.facturation.dto.LigneFacturationPieceResponse;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

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
    public BonDeReception getById(Long id) {
        return bonDeReceptionRepository.findById(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Bon de réception non trouvé avec l'id : " + id));
    }

    @Override
    public org.springframework.data.domain.Page<BonDeReception> getAll(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("id").descending());
        return bonDeReceptionRepository.findAll(pageable);
    }

    @Override
    public List<BonDeReception> getAll() {
        return bonDeReceptionRepository.findAll();
    }

    @Override
    public List<BonDeReception> search(String keyword) {
        return bonDeReceptionRepository.searchBonsDeReception(keyword);
    }

    @Override
    public org.springframework.data.domain.Page<BonDeReception> search(String keyword, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("id").descending());
        return bonDeReceptionRepository.searchBonsDeReception(keyword, pageable);
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
        BonDeReception bl = bonDeReceptionRepository.findById(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Bon de réception non trouvé avec l'id : " + id));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Polices
            Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font fontSousTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font fontTexte = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

            // En-tête
            Paragraph titre = new Paragraph("BON DE RÉCEPTION", fontTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            titre.setSpacingAfter(20);
            document.add(titre);

            // Infos générales
            document.add(new Paragraph("N° : " + bl.getNumero(), fontSousTitre));
            document.add(new Paragraph("Date : " + bl.getDateCreation(), fontTexte));
            if (bl.getAgent() != null) {
                document.add(new Paragraph(
                        "Agent : " + bl.getAgent().getFirstName() + " " + bl.getAgent().getLastName(), fontTexte));
            }

            if (bl.getBonDeCommande() != null) {
                document.add(new Paragraph("Réf. Commande : " + bl.getBonDeCommande().getNumero(), fontTexte));
            }
            document.add(new Paragraph("Kilométrage : " + bl.getKilometrage(), fontTexte));
            document.add(new Paragraph("Remarque : " + (bl.getRemarque() != null ? bl.getRemarque() : ""), fontTexte));

            document.add(new Paragraph(" "));

            // Tableau Pièces
            if (bl.getLignesFacturationPieces() != null && !bl.getLignesFacturationPieces().isEmpty()) {
                document.add(new Paragraph("Pièces :", fontSousTitre));
                document.add(new Paragraph(" "));

                PdfPTable tablePieces = new PdfPTable(4);
                tablePieces.setWidthPercentage(100);
                tablePieces.setWidths(new float[] { 4, 2, 2, 2 });

                PdfPCell cell1 = new PdfPCell(new Phrase("Désignation", fontHeader));
                cell1.setBackgroundColor(Color.DARK_GRAY);
                PdfPCell cell2 = new PdfPCell(new Phrase("Quantité", fontHeader));
                cell2.setBackgroundColor(Color.DARK_GRAY);
                PdfPCell cell3 = new PdfPCell(new Phrase("Prix Unitaire", fontHeader));
                cell3.setBackgroundColor(Color.DARK_GRAY);
                PdfPCell cell4 = new PdfPCell(new Phrase("Total", fontHeader));
                cell4.setBackgroundColor(Color.DARK_GRAY);

                tablePieces.addCell(cell1);
                tablePieces.addCell(cell2);
                tablePieces.addCell(cell3);
                tablePieces.addCell(cell4);

                for (LigneFacturationPiece lp : bl.getLignesFacturationPieces()) {
                    String designation = lp.getPiece() != null ? lp.getPiece().getDesignation() : "-";
                    tablePieces.addCell(new Phrase(designation, fontTexte));
                    tablePieces.addCell(new Phrase(String.valueOf(lp.getQuantite()), fontTexte));
                    tablePieces.addCell(new Phrase(String.valueOf(lp.getPrix()), fontTexte));
                    tablePieces.addCell(new Phrase(String.valueOf(lp.getQuantite() * lp.getPrix()), fontTexte));
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
                    "Total HT : " + bl.getMontantHT() + " FCFA\n" +
                            "TVA : " + bl.getMontantTVA() + " FCFA\n" +
                            "Total TTC : " + bl.getMontantTTC() + " FCFA\n" +
                            "Timbre : " + bl.getMontantTimbre() + " FCFA\n" +
                            "TOTAL GÉNÉRAL : " + bl.getMontantTotal() + " FCFA",
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
}
