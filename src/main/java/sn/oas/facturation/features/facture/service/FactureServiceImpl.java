package sn.oas.facturation.features.facture.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationMainDoeuvre;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationPiece;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreResponse;
import sn.oas.facturation.features.facturation.dto.LigneFacturationPieceResponse;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.dto.FactureResponse;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.data.entity.StockMouvement;
import sn.oas.facturation.features.piecedetache.data.enums.TypeMouvement;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.features.piecedetache.repository.StockMouvementRepository;
import sn.oas.facturation.features.recu.dto.RecuResponse;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.auth.data.entity.Agent;
import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.auth.data.entity.User;
import sn.oas.facturation.features.auth.repository.UserRepository;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneOrdreReparationMainDoeuvre;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneOrdreReparationPiece;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.features.facture.dto.FactureCreateRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import sn.oas.facturation.features.auth.data.enums.Role;
import sn.oas.facturation.features.notification.service.AgentNotificationService;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactureServiceImpl implements FactureService {

    private final FactureRepository factureRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final VehiculeRepository vehiculeRepository;
    private final UserRepository userRepository;
    private final AgentNotificationService agentNotificationService;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;
    private final PieceDetacheRepository pieceDetacheRepository;
    private final StockMouvementRepository stockMouvementRepository;

    @Override
    @Transactional
    public FactureResponse createFacture(FactureCreateRequest request) {
        Client client = (Client) userRepository.findById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable"));
        
        Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                .orElseThrow(() -> new IllegalArgumentException("Véhicule introuvable"));

        OrdreReparation ordreReparation = ordreReparationRepository.findById(request.getOrdreReparationId())
                .orElseThrow(() -> new IllegalArgumentException("Fiche Atelier introuvable"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Agent agent = null;
        if (auth != null && auth.getName() != null) {
            User user = userRepository.findByUsername(auth.getName())
                    .or(() -> userRepository.findByEmail(auth.getName()))
                    .orElse(null);
            agent = (user instanceof Agent) ? (Agent) user : null;
        }

        String numero = documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.FC);

        Facture facture = Facture.builder()
                .numero(numero)
                .client(client)
                .vehicule(vehicule)
                .ordreReparation(ordreReparation)
                .agent(agent)
                .kilometrage(request.getKilometrage() != null ? request.getKilometrage() : 0.0)
                .remarque(request.getRemarque())
                .build();

        BigDecimal ht = BigDecimal.ZERO;
        
        for (LigneOrdreReparationPiece ligneFiche : ordreReparation.getLignesOrdreReparationPieces()) {
            LigneFacturationPiece lfp = LigneFacturationPiece.builder()
                    .facturation(facture)
                    .piece(ligneFiche.getPiece())
                    .quantite(ligneFiche.getQuantite())
                    .prix(ligneFiche.getPrix())
                    .build();
            facture.getLignesFacturationPieces().add(lfp);
            ht = ht.add(BigDecimal.valueOf((long) ligneFiche.getQuantite() * ligneFiche.getPrix()));
        }

        for (LigneOrdreReparationMainDoeuvre ligneFiche : ordreReparation.getLignesOrdreReparationMainDoeuvres()) {
            LigneFacturationMainDoeuvre lfm = LigneFacturationMainDoeuvre.builder()
                    .facturation(facture)
                    .mainDoeuvre(ligneFiche.getMainDoeuvre())
                    .nbreHeure(ligneFiche.getNbreHeure())
                    .tarifHoraire(ligneFiche.getPrix())
                    .build();
            facture.getLignesFacturationMainDoeuvres().add(lfm);
            ht = ht.add(BigDecimal.valueOf((long) ligneFiche.getNbreHeure() * ligneFiche.getPrix()));
        }

        BigDecimal tva = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(request.getAppliquerTVA())) {
            tva = ht.multiply(BigDecimal.valueOf(0.18));
        }

        BigDecimal timbre = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(request.getAppliquerTimbre())) {
            timbre = BigDecimal.valueOf(200);
        }

        BigDecimal ttc = ht.add(tva).add(timbre);

        facture.setMontantHT(ht);
        facture.setMontantTVA(tva);
        facture.setMontantTimbre(timbre);
        facture.setMontantTTC(ttc);
        facture.setMontantTotal(ttc);
        facture.setMontantPaye(BigDecimal.ZERO);
        facture.setResteAPayer(ttc);
        facture.setStatutPaiement(StatutPaiement.NON_PAYE);

        facture = factureRepository.save(facture);

        // Mouvement de stock : La facture diminue le stock réel (SORTIE RÉELLE)
        for (LigneFacturationPiece lfp : facture.getLignesFacturationPieces()) {
            if (lfp.getPiece() != null) {
                PieceDetache p = (PieceDetache) org.hibernate.Hibernate.unproxy(lfp.getPiece());
                if (p instanceof PDP pdp) {
                    double qteReelleAvant = pdp.getQteReelle() != null ? pdp.getQteReelle() : (pdp.getStockMagasin() + pdp.getStockAtelier());
                    pdp.setQteReelle(Math.max(0.0, qteReelleAvant - lfp.getQuantite()));
                    pieceDetacheRepository.save(pdp);

                    stockMouvementRepository.save(StockMouvement.builder()
                            .type(TypeMouvement.SORTIE_REELLE)
                            .quantite((double) lfp.getQuantite())
                            .stockMagasinAvant(pdp.getStockMagasin())
                            .stockAtelierAvant(pdp.getStockAtelier())
                            .stockMagasinApres(pdp.getStockMagasin())
                            .stockAtelierApres(pdp.getStockAtelier())
                            .stockReelApres(pdp.getQteReelle())
                            .prenom(facture.getClient() != null ? facture.getClient().getFirstName() : "")
                            .nom(facture.getClient() != null ? facture.getClient().getLastName() : "")
                            .numDocument(facture.getNumero())
                            .typeDocument("Facture")
                            .numeroSerie(pdp.getReference())
                            .immatriculation(facture.getVehicule() != null ? facture.getVehicule().getImmatriculation() : "")
                            .motif("Facture " + facture.getNumero())
                            .piece(pdp)
                            .agent(agent)
                            .garage(facture.getGarage())
                            .build());
                }
            }
        }

        // Update OrdreReparation status to EN_ATTENTE_PAIEMENT
        if (ordreReparation != null && ordreReparation.getStatut() != StatutOrdreReparation.EN_ATTENTE_PAIEMENT) {
            ordreReparation.setStatut(StatutOrdreReparation.EN_ATTENTE_PAIEMENT);
            ordreReparationRepository.save(ordreReparation);
        }

        agentNotificationService.notifyRole(Role.AGENT, 
            "Nouvelle Facture", 
            "La facture " + facture.getNumero() + " a été générée et est en attente de paiement.");

        return mapToResponse(facture);
    }

    @Override
    @Transactional
    public FactureResponse createFactureAuto(OrdreReparation ordreReparation) {
        Client client = null;
        Vehicule vehicule = ordreReparation.getVehicule();
        if (vehicule != null) {
            client = vehicule.getClient();
        }

        String numero = documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.FC);

        Facture facture = Facture.builder()
                .numero(numero)
                .client(client)
                .vehicule(vehicule)
                .ordreReparation(ordreReparation)
                .agent(null)
                .garage(ordreReparation.getGarage())
                .kilometrage(vehicule != null && vehicule.getKilometrage() != null ? vehicule.getKilometrage() : 0.0)
                .remarque("Facture générée automatiquement depuis la Fiche Atelier " + ordreReparation.getNumero())
                .build();

        BigDecimal ht = BigDecimal.ZERO;
        
        for (LigneOrdreReparationPiece ligneFiche : ordreReparation.getLignesOrdreReparationPieces()) {
            LigneFacturationPiece lfp = LigneFacturationPiece.builder()
                    .facturation(facture)
                    .piece(ligneFiche.getPiece())
                    .quantite(ligneFiche.getQuantite())
                    .prix(ligneFiche.getPrix())
                    .build();
            facture.getLignesFacturationPieces().add(lfp);
            ht = ht.add(BigDecimal.valueOf((long) ligneFiche.getQuantite() * ligneFiche.getPrix()));
        }

        for (LigneOrdreReparationMainDoeuvre ligneFiche : ordreReparation.getLignesOrdreReparationMainDoeuvres()) {
            LigneFacturationMainDoeuvre lfm = LigneFacturationMainDoeuvre.builder()
                    .facturation(facture)
                    .mainDoeuvre(ligneFiche.getMainDoeuvre())
                    .nbreHeure(ligneFiche.getNbreHeure())
                    .tarifHoraire(ligneFiche.getPrix())
                    .build();
            facture.getLignesFacturationMainDoeuvres().add(lfm);
            ht = ht.add(BigDecimal.valueOf((long) ligneFiche.getNbreHeure() * ligneFiche.getPrix()));
        }

        BigDecimal tva = BigDecimal.ZERO;
        BigDecimal timbre = BigDecimal.ZERO;
        BigDecimal ttc = ht.add(tva).add(timbre);

        facture.setMontantHT(ht);
        facture.setMontantTVA(tva);
        facture.setMontantTimbre(timbre);
        facture.setMontantTTC(ttc);
        facture.setMontantTotal(ttc);
        facture.setMontantPaye(BigDecimal.ZERO);
        facture.setResteAPayer(ttc);
        facture.setStatutPaiement(StatutPaiement.NON_PAYE);

        facture = factureRepository.save(facture);

        // Mouvement de stock : La facture diminue le stock réel (SORTIE RÉELLE)
        for (LigneFacturationPiece lfp : facture.getLignesFacturationPieces()) {
            if (lfp.getPiece() != null) {
                PieceDetache p = (PieceDetache) org.hibernate.Hibernate.unproxy(lfp.getPiece());
                if (p instanceof PDP pdp) {
                    double qteReelleAvant = pdp.getQteReelle() != null ? pdp.getQteReelle() : (pdp.getStockMagasin() + pdp.getStockAtelier());
                    pdp.setQteReelle(Math.max(0.0, qteReelleAvant - lfp.getQuantite()));
                    pieceDetacheRepository.save(pdp);

                    stockMouvementRepository.save(StockMouvement.builder()
                            .type(TypeMouvement.SORTIE_REELLE)
                            .quantite((double) lfp.getQuantite())
                            .stockMagasinAvant(pdp.getStockMagasin())
                            .stockAtelierAvant(pdp.getStockAtelier())
                            .stockMagasinApres(pdp.getStockMagasin())
                            .stockAtelierApres(pdp.getStockAtelier())
                            .stockReelApres(pdp.getQteReelle())
                            .prenom(facture.getAgent() != null ? facture.getAgent().getFirstName() : "")
                            .nom(facture.getAgent() != null ? facture.getAgent().getLastName() : "")
                            .numDocument(facture.getNumero())
                            .typeDocument("Facture")
                            .numeroSerie(pdp.getReference())
                            .immatriculation(facture.getVehicule() != null ? facture.getVehicule().getImmatriculation() : "")
                            .motif("Facture auto " + facture.getNumero())
                            .piece(pdp)
                            .agent(facture.getAgent())
                            .garage(facture.getGarage())
                            .build());
                }
            }
        }

        agentNotificationService.notifyRole(Role.AGENT, 
            "Nouvelle Facture Auto", 
            "La facture " + facture.getNumero() + " a été générée automatiquement et est en attente de paiement.");

        return mapToResponse(facture);
    }

    @Override
    @Transactional(readOnly = true)
    public FactureResponse getById(Long id) {
        Facture f = factureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Facture non trouvée avec l'id : " + id));
        return mapToResponse(f);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FactureResponse> getAll(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return factureRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FactureResponse> getAll() {
        return factureRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FactureResponse> search(String keyword) {
        return factureRepository.searchFactures(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FactureResponse> getRecentFactures() {
        return factureRepository.findTop5ByOrderByDateCreationDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!factureRepository.existsById(id)) {
            throw new IllegalArgumentException("Facture non trouvée avec l'id : " + id);
        }
        factureRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id) {
        Facture f = factureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Facture non trouvée avec l'id : " + id));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font fontSousTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font fontTexte = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

            Paragraph titre = new Paragraph("FACTURE FINALE", fontTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            titre.setSpacingAfter(20);
            document.add(titre);

            document.add(new Paragraph("N° : " + f.getNumero(), fontSousTitre));
            document.add(new Paragraph("Date : " + f.getDateCreation(), fontTexte));
            if (f.getAgent() != null) {
                document.add(new Paragraph("Agent : " + f.getAgent().getFirstName() + " " + f.getAgent().getLastName(), fontTexte));
            }
            if (f.getClient() != null) {
                document.add(new Paragraph("Client : " + f.getClient().getFirstName() + " " + f.getClient().getLastName(), fontTexte));
            }
            if (f.getVehicule() != null) {
                Vehicule v = f.getVehicule();
                document.add(new Paragraph("Véhicule : " + v.getMarque() + " " + v.getModele() + " (Immat: " + v.getImmatriculation() + ", Année: " + v.getAnnee() + ")", fontTexte));
            }
            document.add(new Paragraph("Kilométrage : " + f.getKilometrage(), fontTexte));
            if (f.getNumeroBonDeCommande() != null) {
                document.add(new Paragraph("Réf. Bon de Commande : " + f.getNumeroBonDeCommande(), fontTexte));
            }
            document.add(new Paragraph("Remarque : " + (f.getRemarque() != null ? f.getRemarque() : ""), fontTexte));
            
            document.add(new Paragraph(" "));

            if (f.getLignesFacturationPieces() != null && !f.getLignesFacturationPieces().isEmpty()) {
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

                for (LigneFacturationPiece ligne : f.getLignesFacturationPieces()) {
                    String ref = ligne.getPiece() != null ? ligne.getPiece().getDesignation() : "N/A";
                    tablePieces.addCell(new Phrase(ref, fontTexte));
                    tablePieces.addCell(new Phrase(String.valueOf(ligne.getQuantite()), fontTexte));
                    tablePieces.addCell(new Phrase(String.valueOf(ligne.getPrix()), fontTexte));
                    tablePieces.addCell(new Phrase(String.valueOf(ligne.getQuantite() * ligne.getPrix()), fontTexte));
                }
                document.add(tablePieces);
                document.add(new Paragraph(" "));
            }

            if (f.getLignesFacturationMainDoeuvres() != null && !f.getLignesFacturationMainDoeuvres().isEmpty()) {
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

                for (LigneFacturationMainDoeuvre ligne : f.getLignesFacturationMainDoeuvres()) {
                    String cat = ligne.getMainDoeuvre() != null ? ligne.getMainDoeuvre().getCategorie().getNom() : "N/A";
                    tableMo.addCell(new Phrase(cat, fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(ligne.getNbreHeure()), fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(ligne.getTarifHoraire()), fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(ligne.getNbreHeure() * ligne.getTarifHoraire()), fontTexte));
                }
                document.add(tableMo);
                document.add(new Paragraph(" "));
            }

            document.add(new Paragraph("Montant HT : " + f.getMontantHT(), fontSousTitre));
            document.add(new Paragraph("TVA : " + f.getMontantTVA(), fontTexte));
            document.add(new Paragraph("Timbre : " + f.getMontantTimbre(), fontTexte));
            if (f.getMontantAutre() != null && f.getMontantAutre().compareTo(BigDecimal.ZERO) > 0) {
                document.add(new Paragraph("Autre : " + f.getMontantAutre(), fontTexte));
            }
            document.add(new Paragraph("Montant TTC : " + f.getMontantTTC(), fontSousTitre));
            
            Paragraph total = new Paragraph("Montant Total : " + f.getMontantTotal(), fontTitre);
            total.setSpacingBefore(10);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF de la facture", e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FactureResponse> getClientFactures(Client client) {
        return factureRepository.findByClientIdOrderByDateCreationDesc(client.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FactureResponse getClientFactureById(Client client, Long id) {
        Facture f = factureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Facture non trouvée avec l'id : " + id));
        if (f.getClient() == null || !f.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("Accès non autorisé à cette facture");
        }
        return mapToResponse(f);
    }

    private FactureResponse mapToResponse(Facture f) {
        return FactureResponse.builder()
                .id(f.getId())
                .numero(f.getNumero())
                .dateCreation(f.getDateCreation())
                .dateModification(f.getDateModification())
                .montantHT(f.getMontantHT())
                .montantTVA(f.getMontantTVA())
                .montantTTC(f.getMontantTTC())
                .montantTimbre(f.getMontantTimbre())
                .montantAutre(f.getMontantAutre())
                .montantTotal(f.getMontantTotal())
                .montantPaye(f.getMontantPaye())
                .resteAPayer(f.getResteAPayer())
                .statutPaiement(f.getStatutPaiement())
                .agentId(f.getAgent() != null ? f.getAgent().getId() : null)
                .agentNom(f.getAgent() != null ? f.getAgent().getFirstName() + " " + f.getAgent().getLastName() : null)
                .remarque(f.getRemarque())
                .kilometrage(f.getKilometrage())
                .clientId(f.getClient() != null ? f.getClient().getId() : null)
                .clientNom(f.getClient() != null ? f.getClient().getFirstName() + " " + f.getClient().getLastName() : null)
                .vehiculeId(f.getVehicule() != null ? f.getVehicule().getId() : null)
                .immatriculation(f.getVehicule() != null ? f.getVehicule().getImmatriculation() : null)
                .numeroChassis(f.getVehicule() != null ? f.getVehicule().getNumeroChassis() : null)
                .marque(f.getVehicule() != null ? f.getVehicule().getMarque() : null)
                .modele(f.getVehicule() != null ? f.getVehicule().getModele() : null)
                .annee(f.getVehicule() != null ? f.getVehicule().getAnnee() : null)
                .numeroBonDeCommande(f.getNumeroBonDeCommande())
                .ordreReparationId(f.getOrdreReparation() != null ? f.getOrdreReparation().getId() : null)
                .numeroOrdreReparation(f.getOrdreReparation() != null ? f.getOrdreReparation().getNumero() : null)
                .lignesPieces(f.getLignesFacturationPieces() == null ? List.of() : f.getLignesFacturationPieces().stream()
                        .map(lp -> LigneFacturationPieceResponse.builder()
                                .id(lp.getId())
                                .pieceId(lp.getPiece() != null ? lp.getPiece().getId() : null)
                                .designationPiece(lp.getPiece() != null ? lp.getPiece().getDesignation() : null)
                                .quantite(lp.getQuantite())
                                .prix(lp.getPrix())
                                .montantTotal(lp.getQuantite() * lp.getPrix())
                                .build())
                        .collect(Collectors.toList()))
                .lignesMainDoeuvres(f.getLignesFacturationMainDoeuvres() == null ? List.of() : f.getLignesFacturationMainDoeuvres().stream()
                        .map(lm -> LigneFacturationMainDoeuvreResponse.builder()
                                .id(lm.getId())
                                .mainDoeuvreId(lm.getMainDoeuvre() != null ? lm.getMainDoeuvre().getId() : null)
                                .descriptionMainDoeuvre(lm.getMainDoeuvre() != null ? lm.getMainDoeuvre().getCategorie().getNom() : null)
                                .nbreHeure(lm.getNbreHeure())
                                .tarifHoraire(lm.getTarifHoraire())
                                .montantTotal(lm.getNbreHeure() * lm.getTarifHoraire())
                                .build())
                        .collect(Collectors.toList()))
                .recus(f.getRecus() == null ? List.of() : f.getRecus().stream()
                        .map(r -> RecuResponse.builder()
                                .id(r.getId())
                                .numero(r.getNumero())
                                .factureId(f.getId())
                                .montant(r.getMontant())
                                .modePaiement(r.getModePaiement())
                                .remarque(r.getRemarque())
                                .datePaiement(r.getDatePaiement())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
