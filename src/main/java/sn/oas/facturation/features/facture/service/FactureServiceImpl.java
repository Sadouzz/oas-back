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
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.data.entity.PieceMouvement;
import sn.oas.facturation.features.piecedetache.data.enums.TypeMouvement;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.features.piecedetache.repository.PieceMouvementRepository;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.user.data.enums.Role;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.user.repository.UserRepository;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.repository.CompteClientRepository;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneOrdreReparationMainDoeuvre;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneOrdreReparationPiece;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.features.facture.dto.FactureCreateRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import sn.oas.facturation.features.notification.service.AgentNotificationService;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.documentNumber.DocumentType;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactureServiceImpl implements FactureService {

    private final FactureRepository factureRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final VehiculeRepository vehiculeRepository;
    private final UserRepository userRepository;
    private final AgentNotificationService agentNotificationService;
    private final DocumentNumberGeneratorService documentNumberGeneratorService;
    private final PieceDetacheRepository pieceDetacheRepository;
    private final PieceMouvementRepository pieceMouvementRepository;
    private final sn.oas.facturation.features.notification.service.EmailService emailService;
    private final sn.oas.facturation.features.pdfGenerator.service.HtmlToPdfService htmlToPdfService;
    private final sn.oas.facturation.features.pdfTemplate.repository.PdfTemplateRepository pdfTemplateRepository;
    private final CompteClientRepository compteClientRepository;

    @Override
    @Transactional
    public Facture createFacture(FactureCreateRequest request) {
        Client client = (Client) userRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable avec l'id : " + request.getClientId()));
        
        Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable avec l'id : " + request.getVehiculeId()));

        if (vehicule.getClient() == null || !vehicule.getClient().getId().equals(client.getId())) {
            throw new sn.oas.facturation.shared.exception.ForbiddenException("Le véhicule sélectionné n'appartient pas au client de la facture.");
        }

        OrdreReparation ordreReparation = ordreReparationRepository.findById(request.getOrdreReparationId())
                .orElseThrow(() -> new ResourceNotFoundException("Fiche Atelier / Ordre de réparation introuvable avec l'id : " + request.getOrdreReparationId()));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Agent agent = null;
        if (auth != null && auth.getName() != null) {
            User user = userRepository.findByUsername(auth.getName())
                    .or(() -> userRepository.findByEmail(auth.getName()))
                    .orElse(null);
            agent = (user instanceof Agent) ? (Agent) user : null;
        }

        String numero = documentNumberGeneratorService.generateNextNumber(DocumentType.FC);
        var invoiceTemplate = request.getPdfTemplateId() == null ? loadDefaultInvoiceTemplate(request.getPdfLayoutKey()) : loadInvoiceTemplate(request.getPdfTemplateId(), request.getPdfLayoutKey());

        Facture facture = Facture.builder()
                .numero(numero)
                .client(client)
                .vehicule(vehicule)
                .ordreReparation(ordreReparation)
                .agent(agent)
                .kilometrage(request.getKilometrage() != null ? request.getKilometrage() : 0.0)
                .remarque(request.getRemarque())
                .pdfTemplate(invoiceTemplate)
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

        int tauxRemise = tauxRemiseApplicable(client);
        BigDecimal montantRemise = ht.multiply(BigDecimal.valueOf(tauxRemise)).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        ht = ht.subtract(montantRemise);
        facture.setTauxRemiseClient(BigDecimal.valueOf(tauxRemise));
        facture.setMontantRemiseClient(montantRemise);
        if (client.getEcheance() != null && client.getEcheance() > 0) facture.setDateEcheance(java.time.LocalDateTime.now().plusDays(client.getEcheance()));

        BigDecimal tva = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(request.getAppliquerTVA())) {
            tva = ht.multiply(BigDecimal.valueOf(0.18)).setScale(2, java.math.RoundingMode.HALF_UP);
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

                    pieceMouvementRepository.save(PieceMouvement.builder()
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

        // Update OrdreReparation status to PAIEMENT
        if (ordreReparation != null && ordreReparation.getStatut() != StatutOrdreReparation.PAIEMENT) {
            ordreReparation.setStatut(StatutOrdreReparation.PAIEMENT);
            ordreReparationRepository.save(ordreReparation);
        }        agentNotificationService.notifyRole(Role.AGENT, 
            "Nouvelle Facture", 
            "La facture " + facture.getNumero() + " a été générée et est en attente de paiement.");

        if (client.getEmail() != null && !client.getEmail().isEmpty()) {
            byte[] pdfBytes = generatePdf(facture.getId());
            emailService.sendEmailWithAttachment(
                    client.getEmail(),
                    "Votre facture et lien de paiement",
                    "<p>Bonjour,</p><p>Votre facture <b>" + facture.getNumero() + "</b> a été générée. Vous pouvez la trouver en pièce jointe.</p><p>Veuillez procéder au paiement depuis votre espace client pour récupérer votre véhicule.</p><p>Cordialement.</p>",
                    "facture_" + facture.getNumero() + ".pdf",
                    pdfBytes,
                    "application/pdf"
            );
        }

        return facture;
    }

    @Override
    @Transactional
    public Facture createFactureAuto(OrdreReparation ordreReparation) {
        Client client = null;
        Vehicule vehicule = ordreReparation.getVehicule();
        client = ordreReparation.getClient() != null ? ordreReparation.getClient() : (vehicule == null ? null : vehicule.getClient());

        String numero = documentNumberGeneratorService.generateNextNumber(DocumentType.FC);
        var invoiceTemplate = loadDefaultInvoiceTemplate("AVEC_ENTETE");

        Facture facture = Facture.builder()
                .numero(numero)
                .client(client)
                .vehicule(vehicule)
                .ordreReparation(ordreReparation)
                .agent(null)
                .garage(ordreReparation.getGarage())
                .kilometrage(vehicule != null && vehicule.getKilometrage() != null ? vehicule.getKilometrage() : 0.0)
                .remarque("Facture générée automatiquement depuis la Fiche Atelier " + ordreReparation.getNumero())
                .pdfTemplate(invoiceTemplate)
                .build();

        facture = factureRepository.save(facture);

        // Copy pieces
        BigDecimal montantHT = BigDecimal.ZERO;
        List<LigneFacturationPiece> lignesPieces = new ArrayList<>();
        List<LigneFacturationMainDoeuvre> lignesMainDoeuvre = new ArrayList<>();

        if (ordreReparation.getLignesOrdreReparationPieces() != null) {
            for (sn.oas.facturation.features.ordreReparation.data.entity.LigneOrdreReparationPiece pieceUtilisee : ordreReparation.getLignesOrdreReparationPieces()) {
                int q = pieceUtilisee.getQuantite() != null ? pieceUtilisee.getQuantite() : 1;
                int p = pieceUtilisee.getPrix() != null ? pieceUtilisee.getPrix() : 0;
                LigneFacturationPiece lignePiece = LigneFacturationPiece.builder()
                        .facturation(facture)
                        .piece(pieceUtilisee.getPiece())
                        .isCustom(pieceUtilisee.getIsCustom())
                        .designationPds(pieceUtilisee.getDesignationPds())
                        .quantite(q)
                        .prix(p)
                        .build();
                lignesPieces.add(lignePiece);
                montantHT = montantHT.add(BigDecimal.valueOf((long) q * p));
            }
        }

        // Copy MO
        if (ordreReparation.getLignesOrdreReparationMainDoeuvres() != null) {
            for (sn.oas.facturation.features.ordreReparation.data.entity.LigneOrdreReparationMainDoeuvre mo : ordreReparation.getLignesOrdreReparationMainDoeuvres()) {
                int h = mo.getNbreHeure() != null ? mo.getNbreHeure() : 1;
                int t = mo.getPrix() != null ? mo.getPrix() : 0;
                LigneFacturationMainDoeuvre ligneMO = LigneFacturationMainDoeuvre.builder()
                        .facturation(facture)
                        .mainDoeuvre(mo.getMainDoeuvre())
                        .nbreHeure(h)
                        .tarifHoraire(t)
                        .build();
                lignesMainDoeuvre.add(ligneMO);
                montantHT = montantHT.add(BigDecimal.valueOf((long) h * t));
            }
        }

        int tauxRemise = tauxRemiseApplicable(client);
        BigDecimal montantRemise = montantHT.multiply(BigDecimal.valueOf(tauxRemise)).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        montantHT = montantHT.subtract(montantRemise);
        facture.setTauxRemiseClient(BigDecimal.valueOf(tauxRemise));
        facture.setMontantRemiseClient(montantRemise);
        if (client != null && client.getEcheance() != null && client.getEcheance() > 0) facture.setDateEcheance(java.time.LocalDateTime.now().plusDays(client.getEcheance()));
        BigDecimal tva = montantHT.multiply(BigDecimal.valueOf(0.18)).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal timbre = BigDecimal.valueOf(100);
        BigDecimal ttc = montantHT.add(tva).add(timbre);

        facture.setLignesFacturationPieces(lignesPieces);
        facture.setLignesFacturationMainDoeuvres(lignesMainDoeuvre);
        facture.setMontantHT(montantHT);
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

                    pieceMouvementRepository.save(PieceMouvement.builder()
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

        if (client != null && client.getEmail() != null && !client.getEmail().isEmpty()) {
            byte[] pdfBytes = generatePdf(facture.getId());
            emailService.sendEmailWithAttachment(
                    client.getEmail(),
                    "Votre facture et lien de paiement",
                    "<p>Bonjour,</p><p>Votre facture <b>" + facture.getNumero() + "</b> a été générée. Vous pouvez la trouver en pièce jointe.</p><p>Veuillez procéder au paiement depuis votre espace client pour récupérer votre véhicule.</p><p>Cordialement.</p>",
                    "facture_" + facture.getNumero() + ".pdf",
                    pdfBytes,
                    "application/pdf"
            );
        }

        return facture;
    }

    @Override
    @Transactional(readOnly = true)
    public Facture getById(Long id) {
        return factureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture non trouvée avec l'id : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Facture> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return factureRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Facture> getAll() {
        return factureRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Facture> search(String keyword) {
        return factureRepository.searchFactures(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Facture> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return factureRepository.searchFactures(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Facture> getRecentFactures() {
        return factureRepository.findTop5ByOrderByDateCreationDesc();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!factureRepository.existsById(id)) {
            throw new ResourceNotFoundException("Facture non trouvée avec l'id : " + id);
        }
        factureRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id) {
        Facture f = factureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture non trouvée avec l'id : " + id));

        var vehicle = f.getVehicule();
        var tokens = java.util.Map.<String, String>ofEntries(
                java.util.Map.entry("numero", templateValue(f.getNumero())), java.util.Map.entry("date", templateValue(f.getDateCreation())),
                java.util.Map.entry("agentNom", f.getAgent() == null ? "" : templateValue(f.getAgent().getFirstName() + " " + f.getAgent().getLastName())),
                java.util.Map.entry("clientNom", f.getClient() == null ? "" : templateValue(f.getClient().getFirstName() + " " + f.getClient().getLastName())),
                java.util.Map.entry("immatriculation", vehicle == null ? "" : templateValue(vehicle.getImmatriculation())),
                java.util.Map.entry("marque", vehicle == null ? "" : templateValue(vehicle.getMarque())), java.util.Map.entry("modele", vehicle == null ? "" : templateValue(vehicle.getModele())),
                java.util.Map.entry("annee", vehicle == null ? "" : templateValue(vehicle.getAnnee())), java.util.Map.entry("chassis", vehicle == null ? "" : templateValue(vehicle.getNumeroChassis())),
                java.util.Map.entry("numeroBonDeCommande", templateValue(f.getNumeroBonDeCommande())),
                java.util.Map.entry("tauxRemiseClient", templateValue(f.getTauxRemiseClient())),
                java.util.Map.entry("montantRemiseClient", templateValue(f.getMontantRemiseClient())),
                java.util.Map.entry("dateEcheance", templateValue(f.getDateEcheance())),
                java.util.Map.entry("kilometrage", templateValue(f.getKilometrage())), java.util.Map.entry("montantHT", templateValue(f.getMontantHT())),
                java.util.Map.entry("montantTVA", templateValue(f.getMontantTVA())), java.util.Map.entry("montantTimbre", templateValue(f.getMontantTimbre())),
                java.util.Map.entry("montantAutre", templateValue(f.getMontantAutre())), java.util.Map.entry("montantTotal", templateValue(f.getMontantTotal())),
                java.util.Map.entry("montantTTC", templateValue(f.getMontantTTC())), java.util.Map.entry("montantPaye", templateValue(f.getMontantPaye())),
                java.util.Map.entry("resteAPayer", templateValue(f.getResteAPayer())), java.util.Map.entry("remarque", templateValue(f.getRemarque())));
        var tableData = java.util.Map.of("LIGNES_PIECES", f.getLignesFacturationPieces().stream().map(ligne -> java.util.Map.of(
                        "reference", ligne.getPiece() == null ? "" : templateValue(ligne.getPiece().getReference()),
                        "designation", ligne.getPiece() == null ? templateValue(ligne.getDesignationPds()) : templateValue(ligne.getPiece().getDesignation()),
                        "quantite", templateValue(ligne.getQuantite()), "prixUnitaire", templateValue(ligne.getPrix()),
                        "montant", templateValue((long) ligne.getQuantite() * ligne.getPrix()))).toList(),
                "LIGNES_MAIN_DOEUVRE", f.getLignesFacturationMainDoeuvres().stream().map(ligne -> java.util.Map.of(
                        "designation", ligne.getMainDoeuvre() == null ? "" : templateValue(ligne.getMainDoeuvre().getDescription()),
                        "heures", templateValue(ligne.getNbreHeure()), "quantite", templateValue(ligne.getNbreHeure()),
                        "prixUnitaire", templateValue(ligne.getTarifHoraire()), "montant", templateValue((long) ligne.getNbreHeure() * ligne.getTarifHoraire()))).toList());
        byte[] configured = f.getPdfTemplate() == null ? null : htmlToPdfService.genererTemplatePdf(f.getPdfTemplate().getId(), tokens, tableData);
        if (configured != null) return configured;

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

            if (f.getMontantRemiseClient() != null && f.getMontantRemiseClient().signum() > 0) {
                document.add(new Paragraph("Remise client (" + f.getTauxRemiseClient() + " %) : " + f.getMontantRemiseClient(), fontTexte));
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

    private int tauxRemiseApplicable(Client client) {
        if (client == null || client.getTypeClient() != sn.oas.facturation.features.client.data.enums.TypeClient.ENTREPRISE
                || !client.isClientFidele() || client.getMontantRemise() == null) return 0;
        var compte = compteClientRepository.findByClientId(client.getId()).orElse(null);
        return compte != null && compte.isActif() ? client.getMontantRemise() : 0;
    }

    private String templateValue(Object value) { return value == null ? "" : value.toString(); }

    private sn.oas.facturation.features.pdfTemplate.data.entity.PdfTemplate loadInvoiceTemplate(Long id, String layoutKey) {
        var template = pdfTemplateRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Modèle PDF introuvable."));
        String expectedLayout = normalizeInvoiceLayout(layoutKey);
        boolean assignedToInvoice = template.getAssignedDocumentTypes() == null || template.getAssignedDocumentTypes().isEmpty()
                ? "FACTURE".equals(template.getDocumentType()) : template.getAssignedDocumentTypes().contains("FACTURE");
        if (!assignedToInvoice || !template.isActive() || !expectedLayout.equals(normalizeInvoiceLayout(template.getLayoutKey()))) {
            throw new sn.oas.facturation.shared.exception.BadRequestException("Le modèle sélectionné n'est pas actif pour les factures.");
        }
        return template;
    }

    private sn.oas.facturation.features.pdfTemplate.data.entity.PdfTemplate loadDefaultInvoiceTemplate(String layoutKey) {
        return pdfTemplateRepository.findActiveForLayoutByVersion("FACTURE", normalizeInvoiceLayout(layoutKey)).stream().findFirst().orElse(null);
    }

    private String normalizeInvoiceLayout(String layoutKey) {
        if (layoutKey == null || layoutKey.isBlank() || "AVEC_ENTETE".equalsIgnoreCase(layoutKey)) return "AVEC_ENTETE";
        if ("SANS_ENTETE".equalsIgnoreCase(layoutKey)) return "SANS_ENTETE";
        throw new sn.oas.facturation.shared.exception.BadRequestException("Présentation de facture invalide.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Facture> getClientFactures(Client client) {
        return factureRepository.findByClientIdOrderByDateCreationDesc(client.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Facture getClientFactureById(Client client, Long id) {
        Facture f = factureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture non trouvée avec l'id : " + id));
        if (f.getClient() == null || !f.getClient().getId().equals(client.getId())) {
            throw new sn.oas.facturation.shared.exception.ForbiddenException("Accès non autorisé à cette facture");
        }
        return f;
    }
}
