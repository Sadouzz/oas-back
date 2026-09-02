package sn.oas.facturation.features.proforma.service;

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
import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.auth.service.AuthService;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationMainDoeuvre;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationPiece;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreRequest;
import sn.oas.facturation.features.facturation.dto.LigneFacturationPieceRequest;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.facturation.data.enums.StatutFacturation;
import sn.oas.facturation.features.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.features.main_doeuvre.repository.MainDoeuvreRepository;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.features.proforma.data.entity.Proforma;
import sn.oas.facturation.features.proforma.dto.ProformaCreateRequest;
import sn.oas.facturation.features.proforma.dto.ProformaUpdateRequest;
import sn.oas.facturation.features.proforma.repository.ProformaRepository;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.auth.data.enums.Role;
import sn.oas.facturation.features.notification.service.AgentNotificationService;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProformaServiceImpl implements ProformaService {

    private final ProformaRepository proformaRepository;
    private final FactureRepository factureRepository;
    private final ClientRepository clientRepository;
    private final VehiculeRepository vehiculeRepository;
    private final PieceDetacheRepository pieceDetacheRepository;
    private final MainDoeuvreRepository mainDoeuvreRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final AuthService authService;
    private final AgentNotificationService agentNotificationService;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;

    @Override
    @Transactional
    public Proforma create(ProformaCreateRequest request) {
        log.info("Création d'un nouveau proforma");

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client non trouvé avec l'id : " + request.getClientId()));

        Vehicule vehicule;
        if (request.getVehiculeId() != null) {
            vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(() -> new IllegalArgumentException("Véhicule non trouvé avec l'id : " + request.getVehiculeId()));

            if (!vehicule.getClient().getId().equals(client.getId())) {
                throw new IllegalArgumentException("Le véhicule sélectionné n'appartient pas au client sélectionné.");
            }

            // Contrôle kilométrage
            Double currentMileage = vehicule.getKilometrage();
            if (request.getKilometrage() == null || request.getKilometrage() < 0) {
                throw new IllegalArgumentException("Le kilométrage est obligatoire et doit être positif.");
            }
            if (request.getKilometrage() < currentMileage) {
                throw new IllegalArgumentException("Le nouveau kilométrage (" + request.getKilometrage() + 
                        ") ne peut pas être inférieur au kilométrage actuel du véhicule (" + currentMileage + ").");
            }

            vehicule.setKilometrage(request.getKilometrage());
            vehiculeRepository.save(vehicule);
        } else {
            // Création d'un nouveau véhicule pour ce client
            if (request.getImmatriculation() == null || request.getImmatriculation().trim().isEmpty()) {
                throw new IllegalArgumentException("Le numéro d'immatriculation est obligatoire pour ajouter un nouveau véhicule.");
            }
            if (request.getMarque() == null || request.getMarque().trim().isEmpty()) {
                throw new IllegalArgumentException("La marque est obligatoire pour ajouter un nouveau véhicule.");
            }
            if (request.getModele() == null || request.getModele().trim().isEmpty()) {
                throw new IllegalArgumentException("Le modèle est obligatoire pour ajouter un nouveau véhicule.");
            }
            if (request.getAnnee() == null) {
                throw new IllegalArgumentException("L'année est obligatoire pour ajouter un nouveau véhicule.");
            }
            int currentYear = java.time.Year.now().getValue();
            if (request.getAnnee() < 1900 || request.getAnnee() > currentYear + 1) {
                throw new IllegalArgumentException("L'année du véhicule doit être comprise entre 1900 et " + (currentYear + 1) + ".");
            }
            if (vehiculeRepository.existsByImmatriculation(request.getImmatriculation())) {
                throw new IllegalArgumentException("Immatriculation déjà existante : " + request.getImmatriculation());
            }
            if (request.getKilometrage() == null || request.getKilometrage() < 0) {
                throw new IllegalArgumentException("Le kilométrage est obligatoire et doit être positif.");
            }

            vehicule = Vehicule.builder()
                    .immatriculation(request.getImmatriculation())
                    .marque(request.getMarque())
                    .modele(request.getModele())
                    .annee(request.getAnnee())
                    .numeroChassis(request.getNumeroChassis())
                    .kilometrage(request.getKilometrage())
                    .client(client)
                    .build();
            vehicule = vehiculeRepository.save(vehicule);
        }

        // Contrôle présence d'au moins une ligne
        boolean hasPieces = request.getLignesPieces() != null && !request.getLignesPieces().isEmpty();
        boolean hasMainDoeuvres = request.getLignesMainDoeuvres() != null && !request.getLignesMainDoeuvres().isEmpty();
        if (!hasPieces && !hasMainDoeuvres) {
            throw new IllegalArgumentException("Le proforma doit contenir au moins une pièce détachée ou une main d'œuvre.");
        }

        // Contrôle des lignes
        if (hasPieces) {
            for (LigneFacturationPieceRequest p : request.getLignesPieces()) {
                if (p.getQuantite() == null || p.getQuantite() <= 0) {
                    throw new IllegalArgumentException("La quantité de chaque pièce doit être supérieure à 0.");
                }
                if (p.getPrix() == null || p.getPrix() < 0) {
                    throw new IllegalArgumentException("Le prix de chaque pièce doit être positif ou nul.");
                }
            }
        }
        if (hasMainDoeuvres) {
            for (LigneFacturationMainDoeuvreRequest m : request.getLignesMainDoeuvres()) {
                if (m.getNbreHeure() == null || m.getNbreHeure() <= 0) {
                    throw new IllegalArgumentException("Le nombre d'heures de main d'œuvre doit être supérieur à 0.");
                }
                if (m.getTarifHoraire() == null || m.getTarifHoraire() < 0) {
                    throw new IllegalArgumentException("Le tarif horaire de main d'œuvre doit être positif ou nul.");
                }
            }
        }

        BigDecimal montantHT = BigDecimal.ZERO;
        List<LigneFacturationPiece> lignesPieces = new ArrayList<>();
        List<LigneFacturationMainDoeuvre> lignesMainDoeuvres = new ArrayList<>();

        OrdreReparation ordreReparation = null;
        if (request.getOrdreReparationId() != null) {
            ordreReparation = ordreReparationRepository.findById(request.getOrdreReparationId())
                    .orElseThrow(() -> new IllegalArgumentException("Fiche Atelier non trouvée avec l'id : " + request.getOrdreReparationId()));
        }

        Proforma proforma = Proforma.builder()
                .numero(documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.PF))
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .agent(authService.getAgentConnecte())
                // .client(client)
                // .vehicule(vehicule)
                .ordreReparation(ordreReparation)
                .kilometrage(request.getKilometrage())
                .remarque(request.getRemarque())
                // .numeroBonDeCommande(request.getNumeroBonDeCommande())
                .montantHT(BigDecimal.ZERO)
                .montantTVA(BigDecimal.ZERO)
                .montantTTC(BigDecimal.ZERO)
                .montantTimbre(BigDecimal.ZERO)
                // .montantAutre(BigDecimal.ZERO)
                .montantTotal(BigDecimal.ZERO)
                .lignesFacturationPieces(lignesPieces)
                .lignesFacturationMainDoeuvres(lignesMainDoeuvres)
                .build();

        if (hasPieces) {
            for (LigneFacturationPieceRequest p : request.getLignesPieces()) {
                PDP pdp = null;
                Integer prix = p.getPrix();

                if (Boolean.TRUE.equals(p.getIsCustom())) {
                    if (prix == null) prix = 0;
                } else {
                    PieceDetache piece = pieceDetacheRepository.findById(p.getPieceId())
                            .orElseThrow(() -> new RuntimeException("Pièce non trouvée"));
                    pdp = (PDP) org.hibernate.Hibernate.unproxy(piece);
                    if (prix == null) prix = pdp.getPrixUnitaire() != null ? pdp.getPrixUnitaire().intValue() : 0;
                }
                
                LigneFacturationPiece ligne = LigneFacturationPiece.builder()
                        .facturation(proforma)
                        .piece(pdp)
                        .isCustom(Boolean.TRUE.equals(p.getIsCustom()))
                        .designationPds(p.getDesignationPds())
                        .quantite(p.getQuantite())
                        .prix(prix)
                        .build();
                lignesPieces.add(ligne);
                montantHT = montantHT.add(BigDecimal.valueOf((long) p.getQuantite() * prix));
            }
        }

        if (hasMainDoeuvres) {
            for (LigneFacturationMainDoeuvreRequest ligneReq : request.getLignesMainDoeuvres()) {
                MainDoeuvre mainDoeuvre = mainDoeuvreRepository.findById(ligneReq.getMainDoeuvreId())
                        .orElseThrow(() -> new IllegalArgumentException("Main d'œuvre non trouvée avec l'id : " + ligneReq.getMainDoeuvreId()));

                LigneFacturationMainDoeuvre ligne = LigneFacturationMainDoeuvre.builder()
                        .facturation(proforma)
                        .mainDoeuvre(mainDoeuvre)
                        .nbreHeure(ligneReq.getNbreHeure())
                        .tarifHoraire(ligneReq.getTarifHoraire())
                        .build();
                lignesMainDoeuvres.add(ligne);
                montantHT = montantHT.add(BigDecimal.valueOf((long) ligneReq.getNbreHeure() * ligneReq.getTarifHoraire()));
            }
        }

        proforma.setMontantHT(montantHT);
        Double tvaRate = request.getTvaRate() != null ? request.getTvaRate() : 18.0;
        BigDecimal rateBD = BigDecimal.valueOf(tvaRate).divide(BigDecimal.valueOf(100));
        BigDecimal tva = montantHT.multiply(rateBD);
        proforma.setMontantTVA(tva);
        proforma.setMontantTTC(montantHT.add(tva));
        
        BigDecimal timbre = request.getMontantTimbre() != null ? request.getMontantTimbre() : BigDecimal.valueOf(100);
        BigDecimal autre = request.getMontantAutre() != null ? request.getMontantAutre() : BigDecimal.ZERO;
        proforma.setMontantTimbre(timbre);
        // proforma.setMontantAutre(autre);
        proforma.setMontantTotal(proforma.getMontantTTC().add(timbre).add(autre));

        Proforma saved = proformaRepository.save(proforma);

        if (ordreReparation != null) {
            ordreReparation.setStatut(StatutOrdreReparation.EN_ATTENTE_PROFORMA);
            ordreReparationRepository.save(ordreReparation);
        }

        agentNotificationService.notifyRole(Role.AGENT, 
            "Nouveau Proforma", 
            "Le proforma " + saved.getNumero() + " a été généré et est en attente.");

        return saved;
    }

    @Override
    @Transactional
    public Proforma update(Long id, ProformaUpdateRequest request) {
        log.info("Mise à jour du proforma id: {}", id);
        Proforma proforma = proformaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proforma non trouvé avec l'id : " + id));

        Vehicule vehicule = null;
        if (proforma.getOrdreReparation() != null) {
            vehicule = proforma.getOrdreReparation().getVehicule();
        }
        
        if (vehicule != null) {
            if (request.getImmatriculation() != null) vehicule.setImmatriculation(request.getImmatriculation());
            if (request.getMarque() != null) vehicule.setMarque(request.getMarque());
            if (request.getModele() != null) vehicule.setModele(request.getModele());
            if (request.getNumeroChassis() != null) vehicule.setNumeroChassis(request.getNumeroChassis());
        }

        if (request.getAnnee() != null) {
            int currentYear = java.time.Year.now().getValue();
            if (request.getAnnee() < 1900 || request.getAnnee() > currentYear + 1) {
                throw new IllegalArgumentException("L'année du véhicule doit être comprise entre 1900 et " + (currentYear + 1) + ".");
            }
            vehicule.setAnnee(request.getAnnee());
        }

        if (request.getKilometrage() != null) {
            if (request.getKilometrage() < 0) {
                throw new IllegalArgumentException("Le kilométrage doit être positif.");
            }
            if (request.getKilometrage() < vehicule.getKilometrage()) {
                throw new IllegalArgumentException("Le nouveau kilométrage (" + request.getKilometrage() + 
                        ") ne peut pas être inférieur au kilométrage actuel du véhicule (" + vehicule.getKilometrage() + ").");
            }
            vehicule.setKilometrage(request.getKilometrage());
            proforma.setKilometrage(request.getKilometrage());
        }

        if (vehicule != null) vehiculeRepository.save(vehicule);

        // if (request.getNumeroBonDeCommande() != null) proforma.setNumeroBonDeCommande(request.getNumeroBonDeCommande());
        if (request.getRemarque() != null) proforma.setRemarque(request.getRemarque());

        if (request.getLignesPieces() != null || request.getLignesMainDoeuvres() != null) {
            boolean hasPieces = request.getLignesPieces() != null ? !request.getLignesPieces().isEmpty() : !proforma.getLignesFacturationPieces().isEmpty();
            boolean hasMainDoeuvres = request.getLignesMainDoeuvres() != null ? !request.getLignesMainDoeuvres().isEmpty() : !proforma.getLignesFacturationMainDoeuvres().isEmpty();
            if (!hasPieces && !hasMainDoeuvres) {
                throw new IllegalArgumentException("Le proforma doit contenir au moins une pièce détachée ou une main d'œuvre.");
            }

            if (request.getLignesPieces() != null) {
                proforma.getLignesFacturationPieces().clear();
                for (LigneFacturationPieceRequest lReq : request.getLignesPieces()) {
                    if (lReq.getQuantite() == null || lReq.getQuantite() <= 0) {
                        throw new IllegalArgumentException("La quantité de chaque pièce doit être supérieure à 0.");
                    }
                    if (lReq.getPrix() == null && Boolean.FALSE.equals(lReq.getIsCustom())) {
                        throw new IllegalArgumentException("Le prix de chaque pièce doit être positif ou nul.");
                    }
                    
                    PDP pdp = null;
                    Integer prix = lReq.getPrix();
                    
                    if (Boolean.TRUE.equals(lReq.getIsCustom())) {
                        if (prix == null) prix = 0;
                    } else {
                        PieceDetache piece = pieceDetacheRepository.findById(lReq.getPieceId())
                                .orElseThrow(() -> new RuntimeException("Pièce non trouvée"));
                        pdp = (PDP) org.hibernate.Hibernate.unproxy(piece);
                        if (prix == null) prix = pdp.getPrixUnitaire() != null ? pdp.getPrixUnitaire().intValue() : 0;
                    }

                    proforma.getLignesFacturationPieces().add(LigneFacturationPiece.builder()
                            .facturation(proforma)
                            .piece(pdp)
                            .isCustom(Boolean.TRUE.equals(lReq.getIsCustom()))
                            .designationPds(lReq.getDesignationPds())
                            .quantite(lReq.getQuantite())
                            .prix(prix)
                            .build());
                }
            }

            if (request.getLignesMainDoeuvres() != null) {
                proforma.getLignesFacturationMainDoeuvres().clear();
                for (LigneFacturationMainDoeuvreRequest lReq : request.getLignesMainDoeuvres()) {
                    if (lReq.getNbreHeure() == null || lReq.getNbreHeure() <= 0) {
                        throw new IllegalArgumentException("Le nombre d'heures doit être supérieur à 0.");
                    }
                    if (lReq.getTarifHoraire() == null || lReq.getTarifHoraire() < 0) {
                        throw new IllegalArgumentException("Le tarif horaire doit être positif ou nul.");
                    }
                    MainDoeuvre mainDoeuvre = mainDoeuvreRepository.findById(lReq.getMainDoeuvreId())
                            .orElseThrow(() -> new IllegalArgumentException("Main d'œuvre non trouvée avec l'id : " + lReq.getMainDoeuvreId()));
                    proforma.getLignesFacturationMainDoeuvres().add(LigneFacturationMainDoeuvre.builder()
                            .facturation(proforma)
                            .mainDoeuvre(mainDoeuvre)
                            .nbreHeure(lReq.getNbreHeure())
                            .tarifHoraire(lReq.getTarifHoraire())
                            .build());
                }
            }
        }

        BigDecimal montantHT = BigDecimal.ZERO;
        for (LigneFacturationPiece lp : proforma.getLignesFacturationPieces()) {
            montantHT = montantHT.add(BigDecimal.valueOf((long) lp.getQuantite() * lp.getPrix()));
        }
        for (LigneFacturationMainDoeuvre lm : proforma.getLignesFacturationMainDoeuvres()) {
            montantHT = montantHT.add(BigDecimal.valueOf((long) lm.getNbreHeure() * lm.getTarifHoraire()));
        }
        proforma.setMontantHT(montantHT);

        Double tvaRate = request.getTvaRate() != null ? request.getTvaRate() : 18.0;
        BigDecimal rateBD = BigDecimal.valueOf(tvaRate).divide(BigDecimal.valueOf(100));
        BigDecimal tva = montantHT.multiply(rateBD);
        proforma.setMontantTVA(tva);
        proforma.setMontantTTC(montantHT.add(tva));

        if (request.getMontantTimbre() != null) proforma.setMontantTimbre(request.getMontantTimbre());
        // if (request.getMontantAutre() != null) proforma.setMontantAutre(request.getMontantAutre());

        BigDecimal autre = request.getMontantAutre() != null ? request.getMontantAutre() : BigDecimal.ZERO;
        proforma.setMontantTotal(proforma.getMontantTTC().add(proforma.getMontantTimbre()).add(autre));
        proforma.setDateModification(LocalDateTime.now());

        return proformaRepository.save(proforma);
    }

    @Override
    @Transactional(readOnly = true)
    public Proforma getById(Long id) {
        return proformaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proforma non trouvé avec l'id : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Proforma> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return proformaRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proforma> getAll() {
        return proformaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proforma> search(String keyword) {
        return proformaRepository.searchProformas(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Proforma> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return proformaRepository.searchProformas(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Proforma getByOrdreReparationId(Long ordreReparationId) {
        return proformaRepository.findByOrdreReparationId(ordreReparationId)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proforma> getRecentProformas() {
        return proformaRepository.findTop5ByOrderByDateCreationDesc();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!proformaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Proforma non trouvé avec l'id : " + id);
        }
        proformaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Proforma valider(Long id) {
        log.info("Validation du proforma id: {}", id);
        Proforma proforma = proformaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proforma non trouvé avec l'id : " + id));

        proforma.setStatut(StatutFacturation.ACCEPTE);
        
        OrdreReparation ordreReparation = proforma.getOrdreReparation();
        if (ordreReparation != null) {
            ordreReparation.setStatut(StatutOrdreReparation.PROFORMA_VALIDE);
            ordreReparationRepository.save(ordreReparation);
        }

        return proformaRepository.save(proforma);
    }

    @Override
    @Transactional
    public Proforma validerEnvoi(Long id) {
        log.info("Validation et envoi au client du proforma id: {}", id);
        Proforma proforma = proformaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proforma non trouvé avec l'id : " + id));

        proforma.setVisibleClient(true);
        return proformaRepository.save(proforma);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id) {
        Proforma p = proformaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proforma non trouvé avec l'id : " + id));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font fontSousTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font fontTexte = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

            Paragraph titre = new Paragraph("FACTURE PROFORMA", fontTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            titre.setSpacingAfter(20);
            document.add(titre);

            document.add(new Paragraph("N° : " + p.getNumero(), fontSousTitre));
            document.add(new Paragraph("Date : " + p.getDateCreation(), fontTexte));
            if (p.getAgent() != null) {
                document.add(new Paragraph("Agent : " + p.getAgent().getFirstName() + " " + p.getAgent().getLastName(), fontTexte));
            }
            if (p.getOrdreReparation() != null && p.getOrdreReparation().getVehicule() != null) {
                Vehicule v = p.getOrdreReparation().getVehicule();
                if (v.getClient() != null) {
                    document.add(new Paragraph("Client : " + v.getClient().getFirstName() + " " + v.getClient().getLastName(), fontTexte));
                }
                document.add(new Paragraph("Véhicule : " + v.getMarque() + " " + v.getModele() + " (Immat: " + v.getImmatriculation() + ", Année: " + v.getAnnee() + ")", fontTexte));
            }
            document.add(new Paragraph("Kilométrage : " + p.getKilometrage(), fontTexte));
            if (p.getBonDeCommande() != null && p.getBonDeCommande().getNumero() != null) {
                document.add(new Paragraph("Réf. Bon de Commande : " + p.getBonDeCommande().getNumero(), fontTexte));
            }
            document.add(new Paragraph("Remarque : " + (p.getRemarque() != null ? p.getRemarque() : ""), fontTexte));
            
            document.add(new Paragraph(" "));

            if (p.getLignesFacturationPieces() != null && !p.getLignesFacturationPieces().isEmpty()) {
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

                for (LigneFacturationPiece ligne : p.getLignesFacturationPieces()) {
                    String ref = ligne.getPiece() != null ? ligne.getPiece().getDesignation() : "N/A";
                    tablePieces.addCell(new Phrase(ref, fontTexte));
                    tablePieces.addCell(new Phrase(String.valueOf(ligne.getQuantite()), fontTexte));
                    tablePieces.addCell(new Phrase(String.valueOf(ligne.getPrix()), fontTexte));
                    tablePieces.addCell(new Phrase(String.valueOf(ligne.getQuantite() * ligne.getPrix()), fontTexte));
                }
                document.add(tablePieces);
                document.add(new Paragraph(" "));
            }

            if (p.getLignesFacturationMainDoeuvres() != null && !p.getLignesFacturationMainDoeuvres().isEmpty()) {
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

                for (LigneFacturationMainDoeuvre ligne : p.getLignesFacturationMainDoeuvres()) {
                    String cat = ligne.getMainDoeuvre() != null ? ligne.getMainDoeuvre().getCategorie().getNom() : "N/A";
                    tableMo.addCell(new Phrase(cat, fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(ligne.getNbreHeure()), fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(ligne.getTarifHoraire()), fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(ligne.getNbreHeure() * ligne.getTarifHoraire()), fontTexte));
                }
                document.add(tableMo);
                document.add(new Paragraph(" "));
            }

            document.add(new Paragraph("Montant HT : " + p.getMontantHT(), fontSousTitre));
            document.add(new Paragraph("TVA : " + p.getMontantTVA(), fontTexte));
            document.add(new Paragraph("Timbre : " + p.getMontantTimbre(), fontTexte));
            // if (p.getMontantAutre() != null && p.getMontantAutre().compareTo(BigDecimal.ZERO) > 0) {
            //    document.add(new Paragraph("Autre : " + p.getMontantAutre(), fontTexte));
            // }
            document.add(new Paragraph("Montant TTC : " + p.getMontantTTC(), fontSousTitre));
            
            Paragraph total = new Paragraph("Montant Total : " + p.getMontantTotal(), fontTitre);
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

    @Override
    @Transactional
    public Facture convertToFacture(Long id) {
        log.info("Conversion du proforma id: {} en facture finale", id);
        Proforma proforma = proformaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proforma non trouvé avec l'id : " + id));

        Facture facture = Facture.builder()
                .numero("FA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .agent(authService.getAgentConnecte())
                .client(proforma.getOrdreReparation() != null ? proforma.getOrdreReparation().getVehicule().getClient() : null)
                .vehicule(proforma.getOrdreReparation() != null ? proforma.getOrdreReparation().getVehicule() : null)
                .kilometrage(proforma.getKilometrage())
                .remarque(proforma.getRemarque())
                .numeroBonDeCommande(proforma.getBonDeCommande() != null ? proforma.getBonDeCommande().getNumero() : null)
                .montantHT(proforma.getMontantHT())
                .montantTVA(proforma.getMontantTVA())
                .montantTTC(proforma.getMontantTTC())
                .montantTimbre(proforma.getMontantTimbre())
                .montantTotal(proforma.getMontantTotal())
                .lignesFacturationPieces(new ArrayList<>())
                .lignesFacturationMainDoeuvres(new ArrayList<>())
                .build();

        for (LigneFacturationPiece lp : proforma.getLignesFacturationPieces()) {
            facture.getLignesFacturationPieces().add(LigneFacturationPiece.builder()
                    .facturation(facture)
                    .piece(lp.getPiece())
                    .quantite(lp.getQuantite())
                    .prix(lp.getPrix())
                    .build());
        }

        for (LigneFacturationMainDoeuvre lm : proforma.getLignesFacturationMainDoeuvres()) {
            facture.getLignesFacturationMainDoeuvres().add(LigneFacturationMainDoeuvre.builder()
                    .facturation(facture)
                    .mainDoeuvre(lm.getMainDoeuvre())
                    .nbreHeure(lm.getNbreHeure())
                    .tarifHoraire(lm.getTarifHoraire())
                    .build());
        }

        return factureRepository.save(facture);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proforma> getClientProformas(Client client) {
        return proformaRepository.findByClientIdOrderByDateCreationDesc(client.getId());
    }

    @Override
    @Transactional
    public Proforma clientValider(Client client, Long id) {
        log.info("Validation du proforma id: {} par le client: {}", id, client.getId());
        Proforma proforma = proformaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proforma non trouvé avec l'id : " + id));

        if (proforma.getOrdreReparation() == null || proforma.getOrdreReparation().getVehicule() == null ||
            !proforma.getOrdreReparation().getVehicule().getClient().getId().equals(client.getId())) {
            throw new sn.oas.facturation.shared.exception.ForbiddenException("Accès non autorisé à ce proforma");
        }
        if (proforma.getVisibleClient() == null || !proforma.getVisibleClient()) {
            throw new sn.oas.facturation.shared.exception.BadRequestException("Ce proforma n'est pas encore disponible.");
        }

        proforma.setStatut(StatutFacturation.ACCEPTE);

        OrdreReparation ordreReparation = proforma.getOrdreReparation();
        if (ordreReparation != null) {
            ordreReparation.setStatut(StatutOrdreReparation.PROFORMA_VALIDE);
            ordreReparationRepository.save(ordreReparation);
        }

        agentNotificationService.notifyRole(Role.CHEF_ATELIER,
            "Proforma Validé", 
            "Le proforma " + proforma.getNumero() + " a été validé par le client.");

        return proformaRepository.save(proforma);
    }

    @Override
    @Transactional
    public Proforma clientRefuser(Client client, Long id) {
        log.info("Refus du proforma id: {} par le client: {}", id, client.getId());
        Proforma proforma = proformaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proforma non trouvé avec l'id : " + id));

        if (proforma.getOrdreReparation() == null || proforma.getOrdreReparation().getVehicule() == null ||
            !proforma.getOrdreReparation().getVehicule().getClient().getId().equals(client.getId())) {
            throw new sn.oas.facturation.shared.exception.ForbiddenException("Accès non autorisé à ce proforma");
        }
        if (proforma.getVisibleClient() == null || !proforma.getVisibleClient()) {
            throw new sn.oas.facturation.shared.exception.BadRequestException("Ce proforma n'est pas encore disponible.");
        }

        proforma.setStatut(StatutFacturation.REJETE);

        return proformaRepository.save(proforma);
    }
}
