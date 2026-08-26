package sn.oas.facturation.avoirTTC.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.avoirTTC.data.entity.AvoirTTC;
import sn.oas.facturation.avoirTTC.dto.AvoirTTCResponse;
import sn.oas.facturation.avoirTTC.repository.AvoirTTCRepository;
import sn.oas.facturation.facturation.data.entity.LigneFacturationMainDoeuvre;
import sn.oas.facturation.facturation.data.entity.LigneFacturationPiece;
import sn.oas.facturation.facturation.dto.LigneFacturationMainDoeuvreResponse;
import sn.oas.facturation.facturation.dto.LigneFacturationPieceResponse;
import sn.oas.facturation.vehicule.data.entity.Vehicule;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvoirTTCServiceImpl implements AvoirTTCService {

    private final AvoirTTCRepository avoirTTCRepository;

    @Override
    @Transactional(readOnly = true)
    public AvoirTTCResponse getById(Long id) {
        AvoirTTC a = avoirTTCRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Avoir TTC non trouvé avec l'id : " + id));
        return mapToResponse(a);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvoirTTCResponse> getAll() {
        return avoirTTCRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvoirTTCResponse> search(String keyword) {
        return avoirTTCRepository.searchAvoirsTTC(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvoirTTCResponse> getRecentAvoirs() {
        return avoirTTCRepository.findTop5ByOrderByDateCreationDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!avoirTTCRepository.existsById(id)) {
            throw new IllegalArgumentException("Avoir TTC non trouvé avec l'id : " + id);
        }
        avoirTTCRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id) {
        AvoirTTC a = avoirTTCRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Avoir TTC non trouvé avec l'id : " + id));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font fontSousTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font fontTexte = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

            Paragraph titre = new Paragraph("AVOIR TTC", fontTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            titre.setSpacingAfter(20);
            document.add(titre);

            document.add(new Paragraph("N° : " + a.getNumero(), fontSousTitre));
            document.add(new Paragraph("Date : " + a.getDateCreation(), fontTexte));
            if (a.getAgent() != null) {
                document.add(new Paragraph("Agent : " + a.getAgent().getFirstName() + " " + a.getAgent().getLastName(), fontTexte));
            }
            if (a.getOrdreReparation() != null && a.getOrdreReparation().getVehicule() != null) {
                Vehicule v = a.getOrdreReparation().getVehicule();
                if (v.getClient() != null) {
                    document.add(new Paragraph("Client : " + v.getClient().getFirstName() + " " + v.getClient().getLastName(), fontTexte));
                }
                document.add(new Paragraph("Véhicule : " + v.getMarque() + " " + v.getModele() + " (Immat: " + v.getImmatriculation() + ")", fontTexte));
            }
            
            document.add(new Paragraph("Kilométrage : " + a.getKilometrage(), fontTexte));
            if (a.getBonDeCommande() != null) {
                document.add(new Paragraph("Réf. Bon de Commande : " + a.getBonDeCommande().getNumero(), fontTexte));
            }
            document.add(new Paragraph("Remarque : " + (a.getRemarque() != null ? a.getRemarque() : ""), fontTexte));
            
            document.add(new Paragraph(" "));

            if (a.getLignesFacturationPieces() != null && !a.getLignesFacturationPieces().isEmpty()) {
                document.add(new Paragraph("Pièces :", fontSousTitre));
                document.add(new Paragraph(" "));

                PdfPTable tablePieces = new PdfPTable(4);
                tablePieces.setWidthPercentage(100);
                tablePieces.setWidths(new float[]{4f, 2f, 2f, 2f});

                String[] headersPieces = {"Désignation", "Quantité", "Prix Unitaire", "Total"};
                for (String header : headersPieces) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
                    cell.setBackgroundColor(Color.DARK_GRAY);
                    cell.setPadding(5);
                    tablePieces.addCell(cell);
                }

                for (LigneFacturationPiece ligne : a.getLignesFacturationPieces()) {
                    String ref = ligne.getPiece() != null ? ligne.getPiece().getDesignation() : "N/A";
                    tablePieces.addCell(new Phrase(ref, fontTexte));
                    tablePieces.addCell(new Phrase(String.valueOf(ligne.getQuantite()), fontTexte));
                    tablePieces.addCell(new Phrase(String.valueOf(ligne.getPrix()), fontTexte));
                    tablePieces.addCell(new Phrase(String.valueOf(ligne.getQuantite() * ligne.getPrix()), fontTexte));
                }
                document.add(tablePieces);
                document.add(new Paragraph(" "));
            }

            if (a.getLignesFacturationMainDoeuvres() != null && !a.getLignesFacturationMainDoeuvres().isEmpty()) {
                document.add(new Paragraph("Main d'Œuvre :", fontSousTitre));
                document.add(new Paragraph(" "));

                PdfPTable tableMo = new PdfPTable(4);
                tableMo.setWidthPercentage(100);
                tableMo.setWidths(new float[]{4f, 2f, 2f, 2f});

                String[] headersMo = {"Catégorie", "Heures", "Tarif Horaire", "Total"};
                for (String header : headersMo) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
                    cell.setBackgroundColor(Color.DARK_GRAY);
                    cell.setPadding(5);
                    tableMo.addCell(cell);
                }

                for (LigneFacturationMainDoeuvre ligne : a.getLignesFacturationMainDoeuvres()) {
                    String cat = ligne.getMainDoeuvre() != null ? ligne.getMainDoeuvre().getCategorie().getNom() : "N/A";
                    tableMo.addCell(new Phrase(cat, fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(ligne.getNbreHeure()), fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(ligne.getTarifHoraire()), fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(ligne.getNbreHeure() * ligne.getTarifHoraire()), fontTexte));
                }
                document.add(tableMo);
                document.add(new Paragraph(" "));
            }

            document.add(new Paragraph("Montant HT : " + a.getMontantHT(), fontSousTitre));
            document.add(new Paragraph("TVA : " + a.getMontantTVA(), fontTexte));
            document.add(new Paragraph("Timbre : " + a.getMontantTimbre(), fontTexte));
            
            Paragraph totalTTC = new Paragraph("Montant Total TTC : " + a.getMontantTTC(), fontTitre);
            totalTTC.setSpacingBefore(10);
            totalTTC.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalTTC);

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF de l'avoir TTC", e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    private AvoirTTCResponse mapToResponse(AvoirTTC a) {
        return AvoirTTCResponse.builder()
                .id(a.getId())
                .numero(a.getNumero())
                .dateCreation(a.getDateCreation())
                .dateModification(a.getDateModification())
                .montantHT(a.getMontantHT())
                .montantTVA(a.getMontantTVA())
                .montantTTC(a.getMontantTTC())
                .montantTimbre(a.getMontantTimbre())
                .montantTotal(a.getMontantTTC()) // Utiliser montantTTC comme total dans le DTO pour un AvoirTTC
                .agentId(a.getAgent() != null ? a.getAgent().getId() : null)
                .agentNom(a.getAgent() != null ? a.getAgent().getFirstName() + " " + a.getAgent().getLastName() : null)
                .remarque(a.getRemarque())
                .kilometrage(a.getKilometrage())
                .clientId((a.getOrdreReparation() != null && a.getOrdreReparation().getVehicule() != null && a.getOrdreReparation().getVehicule().getClient() != null) ? a.getOrdreReparation().getVehicule().getClient().getId() : null)
                .clientNom((a.getOrdreReparation() != null && a.getOrdreReparation().getVehicule() != null && a.getOrdreReparation().getVehicule().getClient() != null) ? a.getOrdreReparation().getVehicule().getClient().getFirstName() + " " + a.getOrdreReparation().getVehicule().getClient().getLastName() : null)
                .vehiculeId((a.getOrdreReparation() != null && a.getOrdreReparation().getVehicule() != null) ? a.getOrdreReparation().getVehicule().getId() : null)
                .immatriculation((a.getOrdreReparation() != null && a.getOrdreReparation().getVehicule() != null) ? a.getOrdreReparation().getVehicule().getImmatriculation() : null)
                .numeroChassis((a.getOrdreReparation() != null && a.getOrdreReparation().getVehicule() != null) ? a.getOrdreReparation().getVehicule().getNumeroChassis() : null)
                .marque((a.getOrdreReparation() != null && a.getOrdreReparation().getVehicule() != null) ? a.getOrdreReparation().getVehicule().getMarque() : null)
                .modele((a.getOrdreReparation() != null && a.getOrdreReparation().getVehicule() != null) ? a.getOrdreReparation().getVehicule().getModele() : null)
                .annee((a.getOrdreReparation() != null && a.getOrdreReparation().getVehicule() != null) ? a.getOrdreReparation().getVehicule().getAnnee() : null)
                .numeroBonDeCommande(a.getBonDeCommande() != null ? a.getBonDeCommande().getNumero() : null)
                .lignesPieces(a.getLignesFacturationPieces() == null ? List.of() : a.getLignesFacturationPieces().stream()
                        .map(lp -> LigneFacturationPieceResponse.builder()
                                .id(lp.getId())
                                .pieceId(lp.getPiece() != null ? lp.getPiece().getId() : null)
                                .designationPiece(lp.getPiece() != null ? lp.getPiece().getDesignation() : null)
                                .quantite(lp.getQuantite())
                                .prix(lp.getPrix())
                                .montantTotal(lp.getQuantite() * lp.getPrix())
                                .build())
                        .collect(Collectors.toList()))
                .lignesMainDoeuvres(a.getLignesFacturationMainDoeuvres() == null ? List.of() : a.getLignesFacturationMainDoeuvres().stream()
                        .map(lm -> LigneFacturationMainDoeuvreResponse.builder()
                                .id(lm.getId())
                                .mainDoeuvreId(lm.getMainDoeuvre() != null ? lm.getMainDoeuvre().getId() : null)
                                .descriptionMainDoeuvre(lm.getMainDoeuvre() != null ? lm.getMainDoeuvre().getCategorie().getNom() : null)
                                .nbreHeure(lm.getNbreHeure())
                                .tarifHoraire(lm.getTarifHoraire())
                                .montantTotal(lm.getNbreHeure() * lm.getTarifHoraire())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
