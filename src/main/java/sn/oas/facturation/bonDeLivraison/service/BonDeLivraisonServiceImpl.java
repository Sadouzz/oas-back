package sn.oas.facturation.bonDeLivraison.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.auth.data.entity.User;
import sn.oas.facturation.auth.repository.UserRepository;
import sn.oas.facturation.bonDeCommande.data.entity.BonDeCommande;
import sn.oas.facturation.bonDeCommande.repository.BonDeCommandeRepository;
import sn.oas.facturation.bonDeLivraison.data.entity.BonDeLivraison;
import sn.oas.facturation.bonDeLivraison.dto.BonDeLivraisonCreateRequest;
import sn.oas.facturation.bonDeLivraison.dto.BonDeLivraisonResponse;
import sn.oas.facturation.bonDeLivraison.dto.BonDeLivraisonUpdateRequest;
import sn.oas.facturation.bonDeLivraison.repository.BonDeLivraisonRepository;
import sn.oas.facturation.facturation.data.entity.LigneFacturationMainDoeuvre;
import sn.oas.facturation.facturation.data.entity.LigneFacturationPiece;
import sn.oas.facturation.facturation.dto.LigneFacturationMainDoeuvreRequest;
import sn.oas.facturation.facturation.dto.LigneFacturationMainDoeuvreResponse;
import sn.oas.facturation.facturation.dto.LigneFacturationPieceRequest;
import sn.oas.facturation.facturation.dto.LigneFacturationPieceResponse;


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
import sn.oas.facturation.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.main_doeuvre.repository.MainDoeuvreRepository;
import sn.oas.facturation.piecedetache.data.entity.PDP;
import sn.oas.facturation.piecedetache.repository.PieceDetacheRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BonDeLivraisonServiceImpl implements BonDeLivraisonService {

    private final BonDeLivraisonRepository bonDeLivraisonRepository;
    private final BonDeCommandeRepository bonDeCommandeRepository;
    private final UserRepository userRepository;

    private final PieceDetacheRepository pieceDetacheRepository;
    private final MainDoeuvreRepository mainDoeuvreRepository;

    @Override
    @Transactional
    public BonDeLivraisonResponse create(BonDeLivraisonCreateRequest request) {
        log.info("Création d'un nouveau bon de livraison");

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Agent authentifié non trouvé : " + username));
        Agent agent = (user instanceof Agent a) ? a : null;

        BonDeCommande bonDeCommande = null;
        if (request.getBonDeCommandeId() != null) {
            bonDeCommande = bonDeCommandeRepository.findById(request.getBonDeCommandeId())
                    .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));
        }



        BonDeLivraison bonDeLivraison = BonDeLivraison.builder()
                .numero("BL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .agent(agent)
                .bonDeCommande(bonDeCommande)

                .kilometrage(request.getKilometrage())
                .remarque(request.getRemarque())
                .paye(false)
                .lignesFacturationPieces(new ArrayList<>())
                .lignesFacturationMainDoeuvres(new ArrayList<>())
                .build();

        BigDecimal montantHT = BigDecimal.ZERO;

        if (request.getLignesPieces() != null) {
            for (LigneFacturationPieceRequest ligneReq : request.getLignesPieces()) {
                PDP piece = (PDP) pieceDetacheRepository.findById(ligneReq.getPieceId())
                        .orElseThrow(() -> new RuntimeException("Pièce non trouvée"));
                
                LigneFacturationPiece ligne = LigneFacturationPiece.builder()
                        .facturation(bonDeLivraison)
                        .piece(piece)
                        .quantite(ligneReq.getQuantite())
                        .prix(ligneReq.getPrix())
                        .build();
                bonDeLivraison.getLignesFacturationPieces().add(ligne);
                montantHT = montantHT.add(BigDecimal.valueOf((long) ligneReq.getQuantite() * ligneReq.getPrix()));
            }
        }

        if (request.getLignesMainDoeuvres() != null) {
            for (LigneFacturationMainDoeuvreRequest ligneReq : request.getLignesMainDoeuvres()) {
                MainDoeuvre mainDoeuvre = mainDoeuvreRepository.findById(ligneReq.getMainDoeuvreId())
                        .orElseThrow(() -> new RuntimeException("Main d'œuvre non trouvée"));

                LigneFacturationMainDoeuvre ligne = LigneFacturationMainDoeuvre.builder()
                        .facturation(bonDeLivraison)
                        .mainDoeuvre(mainDoeuvre)
                        .nbreHeure(ligneReq.getNbreHeure())
                        .tarifHoraire(ligneReq.getTarifHoraire())
                        .build();
                bonDeLivraison.getLignesFacturationMainDoeuvres().add(ligne);
                montantHT = montantHT.add(BigDecimal.valueOf((long) ligneReq.getNbreHeure() * ligneReq.getTarifHoraire()));
            }
        }

        bonDeLivraison.setMontantHT(montantHT);
        BigDecimal tva = montantHT.multiply(new BigDecimal("0.18"));
        bonDeLivraison.setMontantTVA(tva);
        bonDeLivraison.setMontantTTC(montantHT.add(tva));
        bonDeLivraison.setMontantTimbre(new BigDecimal("100")); // Exemple de timbre fixe
        bonDeLivraison.setMontantTotal(bonDeLivraison.getMontantTTC().add(bonDeLivraison.getMontantTimbre()));

        BonDeLivraison saved = bonDeLivraisonRepository.save(bonDeLivraison);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BonDeLivraisonResponse update(Long id, BonDeLivraisonUpdateRequest request) {
        log.info("Mise à jour du bon de livraison id: {}", id);
        BonDeLivraison bonDeLivraison = bonDeLivraisonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de livraison non trouvé"));

        if (request.getBonDeCommandeId() != null) {
            BonDeCommande bonDeCommande = bonDeCommandeRepository.findById(request.getBonDeCommandeId())
                    .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));
            bonDeLivraison.setBonDeCommande(bonDeCommande);
        }



        bonDeLivraison.setKilometrage(request.getKilometrage());
        bonDeLivraison.setRemarque(request.getRemarque());
        if(request.getPaye() != null) {
            bonDeLivraison.setPaye(request.getPaye());
        }
        bonDeLivraison.setDateModification(LocalDateTime.now());

        bonDeLivraison.getLignesFacturationPieces().clear();
        bonDeLivraison.getLignesFacturationMainDoeuvres().clear();

        BigDecimal montantHT = BigDecimal.ZERO;

        if (request.getLignesPieces() != null) {
            for (LigneFacturationPieceRequest ligneReq : request.getLignesPieces()) {
                PDP piece = (PDP) pieceDetacheRepository.findById(ligneReq.getPieceId())
                        .orElseThrow(() -> new RuntimeException("Pièce non trouvée"));
                
                LigneFacturationPiece ligne = LigneFacturationPiece.builder()
                        .facturation(bonDeLivraison)
                        .piece(piece)
                        .quantite(ligneReq.getQuantite())
                        .prix(ligneReq.getPrix())
                        .build();
                bonDeLivraison.getLignesFacturationPieces().add(ligne);
                montantHT = montantHT.add(BigDecimal.valueOf((long) ligneReq.getQuantite() * ligneReq.getPrix()));
            }
        }

        if (request.getLignesMainDoeuvres() != null) {
            for (LigneFacturationMainDoeuvreRequest ligneReq : request.getLignesMainDoeuvres()) {
                MainDoeuvre mainDoeuvre = mainDoeuvreRepository.findById(ligneReq.getMainDoeuvreId())
                        .orElseThrow(() -> new RuntimeException("Main d'œuvre non trouvée"));

                LigneFacturationMainDoeuvre ligne = LigneFacturationMainDoeuvre.builder()
                        .facturation(bonDeLivraison)
                        .mainDoeuvre(mainDoeuvre)
                        .nbreHeure(ligneReq.getNbreHeure())
                        .tarifHoraire(ligneReq.getTarifHoraire())
                        .build();
                bonDeLivraison.getLignesFacturationMainDoeuvres().add(ligne);
                montantHT = montantHT.add(BigDecimal.valueOf((long) ligneReq.getNbreHeure() * ligneReq.getTarifHoraire()));
            }
        }

        bonDeLivraison.setMontantHT(montantHT);
        BigDecimal tva = montantHT.multiply(new BigDecimal("0.18"));
        bonDeLivraison.setMontantTVA(tva);
        bonDeLivraison.setMontantTTC(montantHT.add(tva));
        bonDeLivraison.setMontantTimbre(new BigDecimal("100"));
        bonDeLivraison.setMontantTotal(bonDeLivraison.getMontantTTC().add(bonDeLivraison.getMontantTimbre()));

        return mapToResponse(bonDeLivraisonRepository.save(bonDeLivraison));
    }

    @Override
    public BonDeLivraisonResponse getById(Long id) {
        BonDeLivraison bonDeLivraison = bonDeLivraisonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de livraison non trouvé"));
        return mapToResponse(bonDeLivraison);
    }

    @Override
    public List<BonDeLivraisonResponse> getAll() {
        return bonDeLivraisonRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BonDeLivraisonResponse> search(String keyword) {
        return bonDeLivraisonRepository.searchBonsDeLivraison(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BonDeLivraisonResponse> getRecentBonsDeLivraison() {
        return bonDeLivraisonRepository.findTop5ByOrderByDateCreationDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!bonDeLivraisonRepository.existsById(id)) {
            throw new RuntimeException("Bon de livraison non trouvé");
        }
        bonDeLivraisonRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id) {
        BonDeLivraison bl = bonDeLivraisonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de livraison non trouvé"));

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
            Paragraph titre = new Paragraph("BON DE LIVRAISON", fontTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            titre.setSpacingAfter(20);
            document.add(titre);

            // Infos générales
            document.add(new Paragraph("N° : " + bl.getNumero(), fontSousTitre));
            document.add(new Paragraph("Date : " + bl.getDateCreation(), fontTexte));
            if (bl.getAgent() != null) {
                document.add(new Paragraph("Agent : " + bl.getAgent().getFirstName() + " " + bl.getAgent().getLastName(), fontTexte));
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
                tablePieces.setWidths(new float[]{4f, 2f, 2f, 2f});

                String[] headersPieces = {"Désignation", "Quantité", "Prix Unitaire", "Total"};
                for (String header : headersPieces) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
                    cell.setBackgroundColor(Color.DARK_GRAY);
                    cell.setPadding(5);
                    tablePieces.addCell(cell);
                }

                for (LigneFacturationPiece ligne : bl.getLignesFacturationPieces()) {
                    String ref = ligne.getPiece() != null ? ligne.getPiece().getReference() : "N/A";
                    tablePieces.addCell(new Phrase(ref, fontTexte));
                    tablePieces.addCell(new Phrase(String.valueOf(ligne.getQuantite()), fontTexte));
                    tablePieces.addCell(new Phrase(String.valueOf(ligne.getPrix()), fontTexte));
                    tablePieces.addCell(new Phrase(String.valueOf(ligne.getQuantite() * ligne.getPrix()), fontTexte));
                }
                document.add(tablePieces);
                document.add(new Paragraph(" "));
            }

            // Tableau Main d'Oeuvre
            if (bl.getLignesFacturationMainDoeuvres() != null && !bl.getLignesFacturationMainDoeuvres().isEmpty()) {
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

                for (LigneFacturationMainDoeuvre ligne : bl.getLignesFacturationMainDoeuvres()) {
                    String cat = ligne.getMainDoeuvre() != null ? ligne.getMainDoeuvre().getCategorie().getNom() : "N/A";
                    tableMo.addCell(new Phrase(cat, fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(ligne.getNbreHeure()), fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(ligne.getTarifHoraire()), fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(ligne.getNbreHeure() * ligne.getTarifHoraire()), fontTexte));
                }
                document.add(tableMo);
                document.add(new Paragraph(" "));
            }

            // Totaux
            document.add(new Paragraph("Montant HT : " + bl.getMontantHT(), fontSousTitre));
            document.add(new Paragraph("TVA : " + bl.getMontantTVA(), fontTexte));
            document.add(new Paragraph("Timbre : " + bl.getMontantTimbre(), fontTexte));
            document.add(new Paragraph("Montant TTC : " + bl.getMontantTTC(), fontSousTitre));
            
            Paragraph total = new Paragraph("Montant Total à Payer : " + bl.getMontantTotal(), fontTitre);
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

    private BonDeLivraisonResponse mapToResponse(BonDeLivraison bl) {
        return BonDeLivraisonResponse.builder()
                .id(bl.getId())
                .numero(bl.getNumero())
                .dateCreation(bl.getDateCreation())
                .dateModification(bl.getDateModification())
                .montantHT(bl.getMontantHT())
                .montantTVA(bl.getMontantTVA())
                .montantTTC(bl.getMontantTTC())
                .montantTimbre(bl.getMontantTimbre())
                .montantTotal(bl.getMontantTotal())
                .agentId(bl.getAgent() != null ? bl.getAgent().getId() : null)
                .agentNom(bl.getAgent() != null ? bl.getAgent().getFirstName() + " " + bl.getAgent().getLastName() : null)
                .remarque(bl.getRemarque())
                .kilometrage(bl.getKilometrage())
                .paye(bl.getPaye())
                .bonDeCommandeId(bl.getBonDeCommande() != null ? bl.getBonDeCommande().getId() : null)
                .bonDeCommandeNumero(bl.getBonDeCommande() != null ? bl.getBonDeCommande().getNumero() : null)

                .lignesPieces(bl.getLignesFacturationPieces().stream().map(ligne -> LigneFacturationPieceResponse.builder()
                        .id(ligne.getId())
                        .pieceId(ligne.getPiece() != null ? ligne.getPiece().getId() : null)
                        .designationPiece(ligne.getPiece() != null ? ligne.getPiece().getReference() : null)
                        .quantite(ligne.getQuantite())
                        .prix(ligne.getPrix())
                        .montantTotal(ligne.getQuantite() * ligne.getPrix())
                        .build()
                ).collect(Collectors.toList()))
                .lignesMainDoeuvres(bl.getLignesFacturationMainDoeuvres().stream().map(ligne -> LigneFacturationMainDoeuvreResponse.builder()
                        .id(ligne.getId())
                        .mainDoeuvreId(ligne.getMainDoeuvre() != null ? ligne.getMainDoeuvre().getId() : null)
                        .descriptionMainDoeuvre(ligne.getMainDoeuvre() != null ? ligne.getMainDoeuvre().getCategorie().getNom() : null)
                        .nbreHeure(ligne.getNbreHeure())
                        .tarifHoraire(ligne.getTarifHoraire())
                        .montantTotal(ligne.getNbreHeure() * ligne.getTarifHoraire())
                        .build()
                ).collect(Collectors.toList()))
                .build();
    }
}
