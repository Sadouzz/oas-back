package sn.oas.facturation.proforma.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.auth.repository.UserRepository;
import sn.oas.facturation.auth.service.AuthService;
import sn.oas.facturation.client.repository.ClientRepository;
import sn.oas.facturation.facturation.data.entity.LigneFacturationMainDoeuvre;
import sn.oas.facturation.facturation.data.entity.LigneFacturationPiece;
import sn.oas.facturation.facturation.dto.LigneFacturationMainDoeuvreRequest;
import sn.oas.facturation.facturation.dto.LigneFacturationMainDoeuvreResponse;
import sn.oas.facturation.facturation.dto.LigneFacturationPieceRequest;
import sn.oas.facturation.facturation.dto.LigneFacturationPieceResponse;
import sn.oas.facturation.facture.data.entity.Facture;
import sn.oas.facturation.facture.dto.FactureResponse;
import sn.oas.facturation.facture.repository.FactureRepository;
import sn.oas.facturation.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.main_doeuvre.repository.MainDoeuvreRepository;
import sn.oas.facturation.piecedetache.data.entity.PDP;
import sn.oas.facturation.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.proforma.data.entity.Proforma;
import sn.oas.facturation.proforma.dto.ProformaCreateRequest;
import sn.oas.facturation.proforma.dto.ProformaResponse;
import sn.oas.facturation.proforma.dto.ProformaUpdateRequest;
import sn.oas.facturation.proforma.repository.ProformaRepository;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import sn.oas.facturation.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.ficheAtelier.data.enums.StatutReparation;

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
    private final FicheAtelierRepository ficheAtelierRepository;
    private final AuthService authService;

    @Override
    @Transactional
    public ProformaResponse create(ProformaCreateRequest request) {
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

        FicheAtelier ficheAtelier = null;
        if (request.getFicheAtelierId() != null) {
            ficheAtelier = ficheAtelierRepository.findById(request.getFicheAtelierId())
                    .orElseThrow(() -> new IllegalArgumentException("Fiche Atelier non trouvée avec l'id : " + request.getFicheAtelierId()));
        }

        Proforma proforma = Proforma.builder()
                .numero("PR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .agent(authService.getAgentConnecte())
                // .client(client)
                // .vehicule(vehicule)
                .ficheAtelier(ficheAtelier)
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
            for (LigneFacturationPieceRequest ligneReq : request.getLignesPieces()) {
                PDP piece = (PDP) pieceDetacheRepository.findById(ligneReq.getPieceId())
                        .orElseThrow(() -> new IllegalArgumentException("Pièce non trouvée avec l'id : " + ligneReq.getPieceId()));
                
                LigneFacturationPiece ligne = LigneFacturationPiece.builder()
                        .facturation(proforma)
                        .piece(piece)
                        .quantite(ligneReq.getQuantite())
                        .prix(ligneReq.getPrix())
                        .build();
                lignesPieces.add(ligne);
                montantHT = montantHT.add(BigDecimal.valueOf((long) ligneReq.getQuantite() * ligneReq.getPrix()));
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

        if (ficheAtelier != null) {
            ficheAtelier.setStatut(StatutReparation.EN_ATTENTE_PROFORMA);
            ficheAtelierRepository.save(ficheAtelier);
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ProformaResponse update(Long id, ProformaUpdateRequest request) {
        log.info("Mise à jour du proforma id: {}", id);
        Proforma proforma = proformaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proforma non trouvé avec l'id : " + id));

        Vehicule vehicule = null;
        if (proforma.getFicheAtelier() != null) {
            vehicule = proforma.getFicheAtelier().getVehicule();
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
                    if (lReq.getPrix() == null || lReq.getPrix() < 0) {
                        throw new IllegalArgumentException("Le prix de chaque pièce doit être positif ou nul.");
                    }
                    PDP piece = (PDP) pieceDetacheRepository.findById(lReq.getPieceId())
                            .orElseThrow(() -> new IllegalArgumentException("Pièce non trouvée avec l'id : " + lReq.getPieceId()));
                    proforma.getLignesFacturationPieces().add(LigneFacturationPiece.builder()
                            .facturation(proforma)
                            .piece(piece)
                            .quantite(lReq.getQuantite())
                            .prix(lReq.getPrix())
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

        return mapToResponse(proformaRepository.save(proforma));
    }

    @Override
    @Transactional(readOnly = true)
    public ProformaResponse getById(Long id) {
        Proforma proforma = proformaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proforma non trouvé avec l'id : " + id));
        return mapToResponse(proforma);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProformaResponse> getAll() {
        return proformaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProformaResponse> search(String keyword) {
        return proformaRepository.searchProformas(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProformaResponse getByFicheAtelierId(Long ficheAtelierId) {
        return proformaRepository.findByFicheAtelierId(ficheAtelierId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProformaResponse> getRecentProformas() {
        return proformaRepository.findTop5ByOrderByDateCreationDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!proformaRepository.existsById(id)) {
            throw new IllegalArgumentException("Proforma non trouvé avec l'id : " + id);
        }
        proformaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ProformaResponse valider(Long id) {
        log.info("Validation du proforma id: {}", id);
        Proforma proforma = proformaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proforma non trouvé avec l'id : " + id));

        proforma.setStatut(sn.oas.facturation.facturation.data.enums.StatutFacturation.ACCEPTE);
        
        FicheAtelier ficheAtelier = proforma.getFicheAtelier();
        if (ficheAtelier != null) {
            ficheAtelier.setStatut(StatutReparation.EN_COURS);
            ficheAtelierRepository.save(ficheAtelier);
        }

        return mapToResponse(proformaRepository.save(proforma));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id) {
        Proforma p = proformaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proforma non trouvé avec l'id : " + id));

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
            if (p.getFicheAtelier() != null && p.getFicheAtelier().getVehicule() != null) {
                Vehicule v = p.getFicheAtelier().getVehicule();
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
                    String ref = ligne.getPiece() != null ? ligne.getPiece().getReference() : "N/A";
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
    public FactureResponse convertToFacture(Long id) {
        log.info("Conversion du proforma id: {} en facture finale", id);
        Proforma proforma = proformaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proforma non trouvé avec l'id : " + id));

        Facture facture = Facture.builder()
                .numero("FA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .agent(authService.getAgentConnecte())
                .client(proforma.getFicheAtelier() != null ? proforma.getFicheAtelier().getVehicule().getClient() : null)
                .vehicule(proforma.getFicheAtelier() != null ? proforma.getFicheAtelier().getVehicule() : null)
                .kilometrage(proforma.getKilometrage())
                .remarque(proforma.getRemarque())
                .numeroBonDeCommande(proforma.getBonDeCommande() != null ? proforma.getBonDeCommande().getNumero() : null)
                .montantHT(proforma.getMontantHT())
                .montantTVA(proforma.getMontantTVA())
                .montantTTC(proforma.getMontantTTC())
                .montantTimbre(proforma.getMontantTimbre())
                // .montantAutre(proforma.getMontantAutre())
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

        Facture savedFacture = factureRepository.save(facture);
        return mapToFactureResponse(savedFacture);
    }

    private ProformaResponse mapToResponse(Proforma p) {
        return ProformaResponse.builder()
                .id(p.getId())
                .numero(p.getNumero())
                .dateCreation(p.getDateCreation())
                .dateModification(p.getDateModification())
                .montantHT(p.getMontantHT())
                .montantTVA(p.getMontantTVA())
                .montantTTC(p.getMontantTTC())
                .montantTimbre(p.getMontantTimbre())
                .montantAutre(BigDecimal.ZERO)
                .montantTotal(p.getMontantTotal())
                .statut(p.getStatut() != null ? p.getStatut().name() : null)
                .agentId(p.getAgent() != null ? p.getAgent().getId() : null)
                .agentNom(p.getAgent() != null ? p.getAgent().getFirstName() + " " + p.getAgent().getLastName() : null)
                .remarque(p.getRemarque())
                .kilometrage(p.getKilometrage())
                .clientId(p.getFicheAtelier() != null && p.getFicheAtelier().getVehicule() != null ? p.getFicheAtelier().getVehicule().getClient().getId() : null)
                .clientNom(p.getFicheAtelier() != null && p.getFicheAtelier().getVehicule() != null ? p.getFicheAtelier().getVehicule().getClient().getFirstName() + " " + p.getFicheAtelier().getVehicule().getClient().getLastName() : null)
                .vehiculeId(p.getFicheAtelier() != null && p.getFicheAtelier().getVehicule() != null ? p.getFicheAtelier().getVehicule().getId() : null)
                .immatriculation(p.getFicheAtelier() != null && p.getFicheAtelier().getVehicule() != null ? p.getFicheAtelier().getVehicule().getImmatriculation() : null)
                .numeroChassis(p.getFicheAtelier() != null && p.getFicheAtelier().getVehicule() != null ? p.getFicheAtelier().getVehicule().getNumeroChassis() : null)
                .marque(p.getFicheAtelier() != null && p.getFicheAtelier().getVehicule() != null ? p.getFicheAtelier().getVehicule().getMarque() : null)
                .modele(p.getFicheAtelier() != null && p.getFicheAtelier().getVehicule() != null ? p.getFicheAtelier().getVehicule().getModele() : null)
                .annee(p.getFicheAtelier() != null && p.getFicheAtelier().getVehicule() != null ? p.getFicheAtelier().getVehicule().getAnnee() : null)
                .numeroBonDeCommande(p.getBonDeCommande() != null ? p.getBonDeCommande().getNumero() : null)
                .lignesPieces(p.getLignesFacturationPieces() == null ? List.of() : p.getLignesFacturationPieces().stream()
                        .map(lp -> LigneFacturationPieceResponse.builder()
                                .id(lp.getId())
                                .pieceId(lp.getPiece() != null ? lp.getPiece().getId() : null)
                                .designationPiece(lp.getPiece() != null ? lp.getPiece().getReference() : null)
                                .quantite(lp.getQuantite())
                                .prix(lp.getPrix())
                                .montantTotal(lp.getQuantite() * lp.getPrix())
                                .build())
                        .collect(Collectors.toList()))
                .lignesMainDoeuvres(p.getLignesFacturationMainDoeuvres() == null ? List.of() : p.getLignesFacturationMainDoeuvres().stream()
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

    private FactureResponse mapToFactureResponse(Facture f) {
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
                .lignesPieces(f.getLignesFacturationPieces() == null ? List.of() : f.getLignesFacturationPieces().stream()
                        .map(lp -> LigneFacturationPieceResponse.builder()
                                .id(lp.getId())
                                .pieceId(lp.getPiece() != null ? lp.getPiece().getId() : null)
                                .designationPiece(lp.getPiece() != null ? lp.getPiece().getReference() : null)
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
                .build();
    }
}
