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
import sn.oas.facturation.features.proforma.data.entity.Proforma;
import sn.oas.facturation.features.proforma.repository.ProformaRepository;
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
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ResourceAlreadyExistsException;
import sn.oas.facturation.features.pdfGenerator.service.OasBrandedPdfPageEvent;

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
    private final ProformaRepository proformaRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final VehiculeRepository vehiculeRepository;
    private final UserRepository userRepository;
    private final AgentNotificationService agentNotificationService;
    private final DocumentNumberGeneratorService documentNumberGeneratorService;
    private final PieceDetacheRepository pieceDetacheRepository;
    private final PieceMouvementRepository pieceMouvementRepository;
    private final sn.oas.facturation.features.notification.service.EmailService emailService;
    private final sn.oas.facturation.features.notification.service.NotificationService notificationService;
    private final sn.oas.facturation.features.pdfGenerator.service.HtmlToPdfService htmlToPdfService;
    private final sn.oas.facturation.features.pdfTemplate.repository.PdfTemplateRepository pdfTemplateRepository;
    private final sn.oas.facturation.features.fiscal.service.FiscalConfigurationService fiscalConfigurationService;

    @Override
    @Transactional
    public Facture createFacture(FactureCreateRequest request) {
        if (request.getOrdreReparationId() == null || request.getClientId() == null || request.getVehiculeId() == null) {
            throw new BadRequestException("Le client, le véhicule et l'ordre de réparation sont obligatoires pour créer une facture.");
        }

        Client client = (Client) userRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable avec l'id : " + request.getClientId()));
        
        Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable avec l'id : " + request.getVehiculeId()));
        sn.oas.facturation.features.vehicule.service.VehiculeActivationPolicy.requireActive(vehicule);

        if (vehicule.getClient() == null || !vehicule.getClient().getId().equals(client.getId())) {
            throw new sn.oas.facturation.shared.exception.ForbiddenException("Le véhicule sélectionné n'appartient pas au client de la facture.");
        }

        OrdreReparation ordreReparation = ordreReparationRepository.findByIdForFacturation(request.getOrdreReparationId())
                .orElseThrow(() -> new ResourceNotFoundException("Fiche Atelier / Ordre de réparation introuvable avec l'id : " + request.getOrdreReparationId()));

        Client clientOrdre = ordreReparation.getClient() != null ? ordreReparation.getClient()
                : ordreReparation.getVehicule() != null ? ordreReparation.getVehicule().getClient() : null;
        if (clientOrdre == null || !clientOrdre.getId().equals(client.getId())) {
            throw new BadRequestException("Le client sélectionné ne correspond pas à l'ordre de réparation.");
        }
        if (ordreReparation.getVehicule() == null || !ordreReparation.getVehicule().getId().equals(vehicule.getId())) {
            throw new BadRequestException("Le véhicule sélectionné ne correspond pas à l'ordre de réparation.");
        }
        if (ordreReparation.getProforma() != null) {
            Proforma proforma = proformaRepository.findByIdForFactureConversion(ordreReparation.getProforma().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proforma introuvable pour cet ordre de réparation."));
            if (proforma.getStatut() != sn.oas.facturation.features.facturation.data.enums.StatutFacturation.ACCEPTE) {
                throw new BadRequestException("Le proforma doit être accepté par le client avant la création de la facture.");
            }
            ordreReparation.setProforma(proforma);
        }
        verifierAucuneFactureExistante(ordreReparation);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Agent agent = null;
        if (auth != null && auth.getName() != null) {
            User user = userRepository.findByUsername(auth.getName())
                    .or(() -> userRepository.findByEmail(auth.getName()))
                    .orElse(null);
            agent = (user instanceof Agent) ? (Agent) user : null;
        }

        String numero = documentNumberGeneratorService.generateNextNumber(DocumentType.FC);
        Proforma proformaSource = ordreReparation.getProforma();
        boolean appliquerTVA = request.getAppliquerTVA() != null ? request.getAppliquerTVA()
                : proformaSource != null
                    ? (proformaSource.getTvaApplicable() != null ? proformaSource.getTvaApplicable()
                        : proformaSource.getMontantTVA() != null && proformaSource.getMontantTVA().signum() > 0)
                    : true;
        var invoiceTemplate = request.getPdfTemplateId() == null ? loadDefaultInvoiceTemplate(request.getPdfLayoutKey()) : loadInvoiceTemplate(request.getPdfTemplateId(), request.getPdfLayoutKey());

        Facture facture = Facture.builder()
                .numero(numero)
                .client(client)
                .vehicule(vehicule)
                .ordreReparation(ordreReparation)
                .proforma(ordreReparation.getProforma())
                .agent(agent)
                .kilometrage(request.getKilometrage() != null ? request.getKilometrage() : 0.0)
                .remarque(request.getRemarque())
                .pdfTemplate(invoiceTemplate)
                .pdfLayoutKey(normalizeInvoiceLayout(request.getPdfLayoutKey()))
                .tvaApplicable(appliquerTVA)
                .build();

        BigDecimal piecesNet = BigDecimal.ZERO;
        BigDecimal mainDoeuvreNet = BigDecimal.ZERO;
        
        for (LigneOrdreReparationPiece ligneFiche : ordreReparation.getLignesOrdreReparationPieces()) {
            LigneFacturationPiece lfp = LigneFacturationPiece.builder()
                    .facturation(facture)
                    .piece(ligneFiche.getPiece())
                    .isCustom(ligneFiche.getIsCustom())
                    .designationPds(ligneFiche.getDesignationPds())
                    .quantite(ligneFiche.getQuantite())
                    .prix(ligneFiche.getPrix())
                    .tauxRemise(ligneFiche.getTauxRemise())
                    .build();
            facture.getLignesFacturationPieces().add(lfp);
            piecesNet = piecesNet.add(montantNet(BigDecimal.valueOf((long) ligneFiche.getQuantite() * ligneFiche.getPrix()), ligneFiche.getTauxRemise()));
        }

        for (LigneOrdreReparationMainDoeuvre ligneFiche : ordreReparation.getLignesOrdreReparationMainDoeuvres()) {
            LigneFacturationMainDoeuvre lfm = LigneFacturationMainDoeuvre.builder()
                    .facturation(facture)
                    .mainDoeuvre(ligneFiche.getMainDoeuvre())
                    .nbreHeure(ligneFiche.getNbreHeure())
                    .tarifHoraire(ligneFiche.getPrix())
                    .tauxRemise(ligneFiche.getTauxRemise())
                    .build();
            facture.getLignesFacturationMainDoeuvres().add(lfm);
            mainDoeuvreNet = mainDoeuvreNet.add(montantNet(BigDecimal.valueOf((long) ligneFiche.getNbreHeure() * ligneFiche.getPrix()), ligneFiche.getTauxRemise()));
        }

        BigDecimal tauxRemise = tauxRemiseClientPourEmission(client, ordreReparation);
        BigDecimal montantRemise = piecesNet.multiply(tauxRemise).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal ht = piecesNet.subtract(montantRemise).add(mainDoeuvreNet);
        facture.setTauxRemiseClient(tauxRemise);
        facture.setMontantRemiseClient(montantRemise);
        if (client.getEcheance() != null && client.getEcheance() > 0) facture.setDateEcheance(java.time.LocalDateTime.now().plusDays(client.getEcheance()));

        var fiscal = fiscalConfigurationService.getCurrent();
        BigDecimal tva = BigDecimal.ZERO;
        if (appliquerTVA) {
            tva = fiscalConfigurationService.calculateRate(ht, fiscal.getTvaRate());
        }

        BigDecimal timbre = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(request.getAppliquerTimbre())) {
            timbre = fiscal.getTimbreFiscal();
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
        marquerProformaConverti(ordreReparation);

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
        if (ordreReparation != null && (ordreReparation.getStatut() == StatutOrdreReparation.ORDRE_DE_TRAVAIL || ordreReparation.getStatut() == StatutOrdreReparation.PROFORMA)) {
            ordreReparation.setStatut(StatutOrdreReparation.PAIEMENT);
            ordreReparationRepository.save(ordreReparation);
        }
        agentNotificationService.notifyRoleInGarage(Role.AGENT, facture.getGarage(),
            "Nouvelle Facture", 
            "La facture " + facture.getNumero() + " a été générée et est en attente de paiement.");
        notificationService.sendInAppNotification(client, "Nouvelle facture",
                "La facture " + facture.getNumero() + " est disponible dans votre espace client.");

        if (client.getEmail() != null && !client.getEmail().isEmpty()) {
            byte[] pdfBytes = generatePdf(facture.getId());
            emailService.sendHtmlEmailWithAttachment(
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
        if (ordreReparation == null || ordreReparation.getId() == null) {
            throw new BadRequestException("Un ordre de réparation valide est obligatoire pour créer une facture.");
        }
        Long ordreId = ordreReparation.getId();
        ordreReparation = ordreReparationRepository.findByIdForFacturation(ordreId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de réparation introuvable avec l'id : " + ordreId));
        if (ordreReparation.getProforma() != null) {
            Proforma proforma = proformaRepository.findByIdForFactureConversion(ordreReparation.getProforma().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proforma introuvable pour cet ordre de réparation."));
            if (proforma.getStatut() != sn.oas.facturation.features.facturation.data.enums.StatutFacturation.ACCEPTE) {
                throw new BadRequestException("Le proforma doit être accepté par le client avant la création de la facture.");
            }
            ordreReparation.setProforma(proforma);
        }
        verifierAucuneFactureExistante(ordreReparation);

        Client client = null;
        Vehicule vehicule = ordreReparation.getVehicule();
        client = ordreReparation.getClient() != null ? ordreReparation.getClient() : (vehicule == null ? null : vehicule.getClient());

        String numero = documentNumberGeneratorService.generateNextNumber(DocumentType.FC);
        var invoiceTemplate = loadDefaultInvoiceTemplate("AVEC_ENTETE");
        Proforma proformaSource = ordreReparation.getProforma();
        boolean appliquerTVA = proformaSource == null || (proformaSource.getTvaApplicable() != null
                ? proformaSource.getTvaApplicable()
                : proformaSource.getMontantTVA() != null && proformaSource.getMontantTVA().signum() > 0);

        Facture facture = Facture.builder()
                .numero(numero)
                .client(client)
                .vehicule(vehicule)
                .ordreReparation(ordreReparation)
                .proforma(ordreReparation.getProforma())
                .agent(null)
                .garage(ordreReparation.getGarage())
                .kilometrage(vehicule != null && vehicule.getKilometrage() != null ? vehicule.getKilometrage() : 0.0)
                .remarque("Facture générée automatiquement depuis la Fiche Atelier " + ordreReparation.getNumero())
                .pdfTemplate(invoiceTemplate)
                .pdfLayoutKey("AVEC_ENTETE")
                .tvaApplicable(appliquerTVA)
                .build();

        facture = factureRepository.save(facture);
        marquerProformaConverti(ordreReparation);

        // Copy pieces
        BigDecimal montantPiecesNet = BigDecimal.ZERO;
        BigDecimal montantMoNet = BigDecimal.ZERO;
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
                        .tauxRemise(pieceUtilisee.getTauxRemise())
                        .build();
                lignesPieces.add(lignePiece);
                montantPiecesNet = montantPiecesNet.add(montantNet(BigDecimal.valueOf((long) q * p), pieceUtilisee.getTauxRemise()));
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
                        .tauxRemise(mo.getTauxRemise())
                        .build();
                lignesMainDoeuvre.add(ligneMO);
                montantMoNet = montantMoNet.add(montantNet(BigDecimal.valueOf((long) h * t), mo.getTauxRemise()));
            }
        }

        BigDecimal tauxRemise = tauxRemiseClientPourEmission(client, ordreReparation);
        BigDecimal montantRemise = montantPiecesNet.multiply(tauxRemise).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal montantHT = montantPiecesNet.subtract(montantRemise).add(montantMoNet);
        facture.setTauxRemiseClient(tauxRemise);
        facture.setMontantRemiseClient(montantRemise);
        if (client != null && client.getEcheance() != null && client.getEcheance() > 0) facture.setDateEcheance(java.time.LocalDateTime.now().plusDays(client.getEcheance()));
        var fiscal = fiscalConfigurationService.getCurrent();
        BigDecimal tva = appliquerTVA ? fiscalConfigurationService.calculateRate(montantHT, fiscal.getTvaRate()) : BigDecimal.ZERO;
        BigDecimal timbre = fiscal.getTimbreFiscal();
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

        agentNotificationService.notifyRoleInGarage(Role.AGENT, facture.getGarage(),
            "Nouvelle Facture Auto", 
            "La facture " + facture.getNumero() + " a été générée automatiquement et est en attente de paiement.");

        if (client != null) {
            notificationService.sendInAppNotification(client, "Nouvelle facture",
                    "La facture " + facture.getNumero() + " est disponible dans votre espace client.");
        }
        if (client != null && client.getEmail() != null && !client.getEmail().isEmpty()) {
            byte[] pdfBytes = generatePdf(facture.getId());
            emailService.sendHtmlEmailWithAttachment(
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
        return generateInvoicePdf(id, null);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id, String layoutKey) {
        return generateInvoicePdf(id, layoutKey);
    }

    private byte[] generateInvoicePdf(Long id, String requestedLayoutKey) {
        Facture f = factureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture non trouvée avec l'id : " + id));

        String layoutKey = normalizeInvoiceLayout(requestedLayoutKey != null ? requestedLayoutKey
                : f.getPdfTemplate() != null ? f.getPdfTemplate().getLayoutKey() : f.getPdfLayoutKey());

        var vehicle = f.getVehicule();
        BigDecimal montantRemiseGenerale = calculerRemiseGenerale(f);
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
                java.util.Map.entry("montantRemiseGenerale", templateValue(montantRemiseGenerale)),
                java.util.Map.entry("dateEcheance", templateValue(f.getDateEcheance())),
                java.util.Map.entry("kilometrage", templateValue(f.getKilometrage())), java.util.Map.entry("montantHT", templateValue(f.getMontantHT())),
                java.util.Map.entry("montantTVA", Boolean.FALSE.equals(f.getTvaApplicable()) ? "Non appliquée" : templateValue(f.getMontantTVA())),
                java.util.Map.entry("tvaApplicable", Boolean.TRUE.equals(f.getTvaApplicable()) ? "Oui" : "Non"), java.util.Map.entry("montantTimbre", templateValue(f.getMontantTimbre())),
                java.util.Map.entry("montantAutre", templateValue(f.getMontantAutre())), java.util.Map.entry("montantTotal", templateValue(f.getMontantTotal())),
                java.util.Map.entry("montantTTC", templateValue(f.getMontantTTC())), java.util.Map.entry("montantPaye", templateValue(f.getMontantPaye())),
                java.util.Map.entry("resteAPayer", templateValue(f.getResteAPayer())), java.util.Map.entry("remarque", templateValue(f.getRemarque())));
        var tableData = java.util.Map.of("LIGNES_PIECES", f.getLignesFacturationPieces().stream().map(ligne -> {
                    BigDecimal brut = BigDecimal.valueOf((long) ligne.getQuantite() * ligne.getPrix());
                    BigDecimal net = montantNet(brut, ligne.getTauxRemise());
                    return java.util.Map.of("reference", ligne.getPiece() == null ? "" : templateValue(ligne.getPiece().getReference()),
                            "designation", ligne.getPiece() == null ? templateValue(ligne.getDesignationPds()) : templateValue(ligne.getPiece().getDesignation()),
                            "quantite", templateValue(ligne.getQuantite()), "prixUnitaire", templateValue(ligne.getPrix()),
                            "tauxRemise", templateValue(ligne.getTauxRemise()), "montantBrut", templateValue(brut),
                            "montantRemise", templateValue(brut.subtract(net)), "montant", templateValue(net));
                }).toList(),
                "LIGNES_MAIN_DOEUVRE", f.getLignesFacturationMainDoeuvres().stream().map(ligne -> {
                    BigDecimal brut = BigDecimal.valueOf((long) ligne.getNbreHeure() * ligne.getTarifHoraire());
                    BigDecimal net = montantNet(brut, ligne.getTauxRemise());
                    return java.util.Map.of("designation", ligne.getMainDoeuvre() == null ? "" : templateValue(ligne.getMainDoeuvre().getDescription()),
                            "heures", templateValue(ligne.getNbreHeure()), "quantite", templateValue(ligne.getNbreHeure()),
                            "prixUnitaire", templateValue(ligne.getTarifHoraire()), "tauxRemise", templateValue(ligne.getTauxRemise()),
                            "montantBrut", templateValue(brut), "montantRemise", templateValue(brut.subtract(net)), "montant", templateValue(net));
                }).toList());
        var selectedTemplate = requestedLayoutKey == null ? f.getPdfTemplate() : loadDefaultInvoiceTemplate(layoutKey);
        byte[] configured = selectedTemplate == null ? null : htmlToPdfService.genererTemplatePdf(selectedTemplate.getId(), tokens, tableData);
        if (configured != null) return configured;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean branded = "AVEC_ENTETE".equals(layoutKey);
        Document document = branded
                ? new Document(com.lowagie.text.PageSize.A4, 34, 34, 105, 78)
                : new Document(com.lowagie.text.PageSize.A4, 40, 40, 40, 40);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            if (branded) writer.setPageEvent(new OasBrandedPdfPageEvent());
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

                PdfPTable tablePieces = new PdfPTable(5);
                tablePieces.setWidthPercentage(100);
                tablePieces.setWidths(new float[]{4f, 1.2f, 1.8f, 1.5f, 2f});

                String[] headersPieces = {"Désignation", "Quantité", "Prix Unitaire", "Remise (%)", "Total net"};
                for (String header : headersPieces) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
                    cell.setBackgroundColor(Color.DARK_GRAY);
                    cell.setPadding(5);
                    tablePieces.addCell(cell);
                }

                for (LigneFacturationPiece ligne : f.getLignesFacturationPieces()) {
                    String ref = ligne.getPiece() != null ? ligne.getPiece().getDesignation() : "N/A";
                    BigDecimal brut = BigDecimal.valueOf((long) ligne.getQuantite() * ligne.getPrix());
                    BigDecimal net = montantNet(brut, ligne.getTauxRemise());
                    tablePieces.addCell(new Phrase(ref, fontTexte));
                    tablePieces.addCell(new Phrase(String.valueOf(ligne.getQuantite()), fontTexte));
                    tablePieces.addCell(new Phrase(String.valueOf(ligne.getPrix()), fontTexte));
                    tablePieces.addCell(new Phrase(String.valueOf(ligne.getTauxRemise() == null ? BigDecimal.ZERO : ligne.getTauxRemise()), fontTexte));
                    tablePieces.addCell(new Phrase(net.stripTrailingZeros().toPlainString(), fontTexte));
                }
                document.add(tablePieces);
                document.add(new Paragraph(" "));
            }

            if (f.getLignesFacturationMainDoeuvres() != null && !f.getLignesFacturationMainDoeuvres().isEmpty()) {
                document.add(new Paragraph("Main d'Œuvre :", fontSousTitre));
                document.add(new Paragraph(" "));

                PdfPTable tableMo = new PdfPTable(5);
                tableMo.setWidthPercentage(100);
                tableMo.setWidths(new float[]{4f, 1.2f, 1.8f, 1.5f, 2f});

                String[] headersMo = {"Catégorie", "Heures", "Tarif Horaire", "Remise (%)", "Total net"};
                for (String header : headersMo) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
                    cell.setBackgroundColor(Color.DARK_GRAY);
                    cell.setPadding(5);
                    tableMo.addCell(cell);
                }

                for (LigneFacturationMainDoeuvre ligne : f.getLignesFacturationMainDoeuvres()) {
                    String cat = ligne.getMainDoeuvre() != null ? ligne.getMainDoeuvre().getCategorie().getNom() : "N/A";
                    BigDecimal brut = BigDecimal.valueOf((long) ligne.getNbreHeure() * ligne.getTarifHoraire());
                    BigDecimal net = montantNet(brut, ligne.getTauxRemise());
                    tableMo.addCell(new Phrase(cat, fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(ligne.getNbreHeure()), fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(ligne.getTarifHoraire()), fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(ligne.getTauxRemise() == null ? BigDecimal.ZERO : ligne.getTauxRemise()), fontTexte));
                    tableMo.addCell(new Phrase(net.stripTrailingZeros().toPlainString(), fontTexte));
                }
                document.add(tableMo);
                document.add(new Paragraph(" "));
            }

            if (f.getMontantRemiseClient() != null && f.getMontantRemiseClient().signum() > 0) {
                document.add(new Paragraph("Remise compte client sur les pièces (" + f.getTauxRemiseClient() + " %) : " + f.getMontantRemiseClient(), fontTexte));
            }
            if (montantRemiseGenerale.signum() > 0) document.add(new Paragraph("Remises générales appliquées par ligne : " + montantRemiseGenerale, fontTexte));
            document.add(new Paragraph("Montant HT : " + f.getMontantHT(), fontSousTitre));
            document.add(new Paragraph("TVA : " + (Boolean.FALSE.equals(f.getTvaApplicable()) ? "Non appliquée" : f.getMontantTVA()), fontTexte));
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
        if (client == null || client.getMontantRemise() == null) return 0;
        return Math.max(0, Math.min(100, client.getMontantRemise()));
    }

    /**
     * La proforma liée à l'ordre conserve le taux de remise compte client appliqué lors de son émission.
     * Une facture émise ensuite reprend ce taux historique afin qu'une modification ultérieure du compte
     * client ne change pas le prix du même dossier. Sans proforma liée, le taux courant reste la source.
     */
    private BigDecimal tauxRemiseClientPourEmission(Client client, OrdreReparation ordreReparation) {
        if (ordreReparation != null && ordreReparation.getProforma() != null
                && ordreReparation.getProforma().getTauxRemiseClient() != null) {
            return ordreReparation.getProforma().getTauxRemiseClient();
        }
        return BigDecimal.valueOf(tauxRemiseApplicable(client));
    }

    private void verifierAucuneFactureExistante(OrdreReparation ordreReparation) {
        if (ordreReparation != null && ordreReparation.getId() != null
                && factureRepository.findFirstByOrdreReparationId(ordreReparation.getId()).isPresent()) {
            throw new ResourceAlreadyExistsException("Une facture existe déjà pour l'ordre de réparation "
                    + ordreReparation.getNumero() + ".");
        }
        if (ordreReparation != null && ordreReparation.getProforma() != null
                && factureRepository.findFirstByProformaId(ordreReparation.getProforma().getId()).isPresent()) {
            throw new ResourceAlreadyExistsException("Ce proforma a déjà été converti en facture.");
        }
    }

    private void marquerProformaConverti(OrdreReparation ordreReparation) {
        if (ordreReparation != null && ordreReparation.getProforma() != null) {
            ordreReparation.getProforma().setStatut(
                    sn.oas.facturation.features.facturation.data.enums.StatutFacturation.CONVERTIE);
        }
    }

    private BigDecimal montantNet(BigDecimal brut, BigDecimal tauxRemise) {
        BigDecimal taux = tauxRemise == null ? BigDecimal.ZERO : tauxRemise;
        return brut.multiply(BigDecimal.valueOf(100).subtract(taux))
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal calculerRemiseGenerale(Facture facture) {
        BigDecimal remise = BigDecimal.ZERO;
        if (facture.getLignesFacturationPieces() != null) {
            for (LigneFacturationPiece ligne : facture.getLignesFacturationPieces()) {
                BigDecimal brut = BigDecimal.valueOf((long) ligne.getQuantite() * ligne.getPrix());
                remise = remise.add(brut.subtract(montantNet(brut, ligne.getTauxRemise())));
            }
        }
        if (facture.getLignesFacturationMainDoeuvres() != null) {
            for (LigneFacturationMainDoeuvre ligne : facture.getLignesFacturationMainDoeuvres()) {
                BigDecimal brut = BigDecimal.valueOf((long) ligne.getNbreHeure() * ligne.getTarifHoraire());
                remise = remise.add(brut.subtract(montantNet(brut, ligne.getTauxRemise())));
            }
        }
        return remise;
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
