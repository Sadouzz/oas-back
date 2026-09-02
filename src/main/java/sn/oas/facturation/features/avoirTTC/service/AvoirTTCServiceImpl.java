package sn.oas.facturation.features.avoirTTC.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.auth.data.entity.Agent;
import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.auth.data.entity.User;
import sn.oas.facturation.features.auth.repository.UserRepository;
import sn.oas.facturation.features.avoirTTC.data.entity.AvoirTTC;
import sn.oas.facturation.features.avoirTTC.dto.AvoirTTCCreateRequest;
import sn.oas.facturation.features.avoirTTC.dto.AvoirTTCResponse;
import sn.oas.facturation.features.avoirTTC.repository.AvoirTTCRepository;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationMainDoeuvre;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationPiece;
import sn.oas.facturation.features.facturation.data.enums.StatutFacturation;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreRequest;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreResponse;
import sn.oas.facturation.features.facturation.dto.LigneFacturationPieceRequest;
import sn.oas.facturation.features.facturation.dto.LigneFacturationPieceResponse;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.garage.repository.GarageRepository;
import sn.oas.facturation.features.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.features.main_doeuvre.repository.MainDoeuvreRepository;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.data.entity.StockMouvement;
import sn.oas.facturation.features.piecedetache.data.enums.TypeMouvement;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.features.piecedetache.repository.StockMouvementRepository;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.documentNumber.DocumentType;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvoirTTCServiceImpl implements AvoirTTCService {

    private final AvoirTTCRepository avoirTTCRepository;
    private final UserRepository userRepository;
    private final VehiculeRepository vehiculeRepository;
    private final PieceDetacheRepository pieceDetacheRepository;
    private final StockMouvementRepository stockMouvementRepository;
    private final MainDoeuvreRepository mainDoeuvreRepository;
    private final GarageRepository garageRepository;
    private final DocumentNumberGeneratorService documentNumberGeneratorService;

    private Agent getAgentConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            User user = userRepository.findByUsername(auth.getName())
                    .or(() -> userRepository.findByEmail(auth.getName()))
                    .orElse(null);
            return (user instanceof Agent) ? (Agent) user : null;
        }
        return null;
    }

    private Garage getGarageConnecte(Agent agent) {
        if (agent != null && agent.getGarage() != null) {
            return agent.getGarage();
        }
        org.springframework.web.context.request.ServletRequestAttributes attributes = 
                (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String garageIdHeader = attributes.getRequest().getHeader("X-Garage-ID");
            if (garageIdHeader != null && !garageIdHeader.isEmpty()) {
                try {
                    Garage g = garageRepository.findById(Long.parseLong(garageIdHeader)).orElse(null);
                    if (g != null) return g;
                } catch (Exception ignored) {}
            }
        }
        Garage g = documentNumberGeneratorService.getCurrentGarage();
        if (g != null) return g;
        return garageRepository.findAll().stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public AvoirTTCResponse create(AvoirTTCCreateRequest request) {
        Client client = null;
        if (request.getClientId() != null) {
            client = (Client) userRepository.findById(request.getClientId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Client introuvable avec l'id : " + request.getClientId()));
        }

        Vehicule vehicule = null;
        if (request.getVehiculeId() != null) {
            vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Véhicule introuvable avec l'id : " + request.getVehiculeId()));
        }

        Agent agent = getAgentConnecte();
        Garage garage = getGarageConnecte(agent);
        String numero = documentNumberGeneratorService.generateNextNumber(garage, DocumentType.AT);

        BigDecimal montantHT = BigDecimal.ZERO;
        List<LigneFacturationPiece> lignesPieces = new ArrayList<>();
        List<LigneFacturationMainDoeuvre> lignesMainDoeuvre = new ArrayList<>();

        AvoirTTC avoirTTC = AvoirTTC.builder()
                .numero(numero)
                .client(client)
                .vehicule(vehicule)
                .agent(agent)
                .garage(garage)
                .kilometrage(request.getKilometrage() != null ? request.getKilometrage() : 0.0)
                .remarque(request.getRemarque())
                .statut(StatutFacturation.ACCEPTE)
                .montantTotal(BigDecimal.ZERO)
                .montantHT(BigDecimal.ZERO)
                .montantTVA(BigDecimal.ZERO)
                .montantTimbre(BigDecimal.ZERO)
                .montantTTC(BigDecimal.ZERO)
                .build();

        if (request.getLignesPieces() != null) {
            for (LigneFacturationPieceRequest lpReq : request.getLignesPieces()) {
                PDP pdp = null;
                String designationPds = lpReq.getDesignationPds();
                boolean isCustom = Boolean.TRUE.equals(lpReq.getIsCustom()) || lpReq.getPieceId() == null;

                if (lpReq.getPieceId() != null) {
                    PieceDetache pd = pieceDetacheRepository.findById(lpReq.getPieceId()).orElse(null);
                    if (pd instanceof PDP p) {
                        pdp = p;
                        if (designationPds == null || designationPds.isBlank()) {
                            designationPds = pdp.getDesignation();
                        }
                    }
                }

                int quantite = lpReq.getQuantite() != null ? lpReq.getQuantite() : 1;
                int prix = lpReq.getPrix() != null ? lpReq.getPrix() : 0;

                LigneFacturationPiece lfp = LigneFacturationPiece.builder()
                        .facturation(avoirTTC)
                        .piece(pdp)
                        .isCustom(isCustom)
                        .designationPds(designationPds)
                        .quantite(quantite)
                        .prix(prix)
                        .build();

                lignesPieces.add(lfp);
                montantHT = montantHT.add(BigDecimal.valueOf((long) quantite * prix));

                // Mouvement de stock : Augmentation du stock magasin et stock réel (ENTRÉE)
                if (pdp != null) {
                    Double magasinAvant = pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0.0;
                    Double atelierAvant = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0.0;

                    pdp.setStockMagasin(magasinAvant + quantite);
                    pdp.setQteReelle(pdp.getStockMagasin() + pdp.getStockAtelier());
                    pieceDetacheRepository.save(pdp);

                    stockMouvementRepository.save(StockMouvement.builder()
                            .type(TypeMouvement.ENTREE)
                            .quantite((double) quantite)
                            .stockMagasinAvant(magasinAvant)
                            .stockAtelierAvant(atelierAvant)
                            .stockMagasinApres(pdp.getStockMagasin())
                            .stockAtelierApres(pdp.getStockAtelier())
                            .stockReelApres(pdp.getQteReelle())
                            .prenom(agent != null ? agent.getFirstName() : "")
                            .nom(agent != null ? agent.getLastName() : "")
                            .numDocument(numero)
                            .typeDocument("Avoir TTC")
                            .numeroSerie(pdp.getReference())
                            .immatriculation(vehicule != null ? vehicule.getImmatriculation() : "")
                            .motif("Retour pièce Avoir " + numero)
                            .piece(pdp)
                            .agent(agent)
                            .garage(avoirTTC.getGarage())
                            .build());
                }
            }
        }

        if (request.getLignesMainDoeuvres() != null) {
            for (LigneFacturationMainDoeuvreRequest lmReq : request.getLignesMainDoeuvres()) {
                MainDoeuvre md = null;
                if (lmReq.getMainDoeuvreId() != null) {
                    md = mainDoeuvreRepository.findById(lmReq.getMainDoeuvreId()).orElse(null);
                }

                int nbreHeure = lmReq.getNbreHeure() != null ? lmReq.getNbreHeure() : 1;
                int tarifHoraire = lmReq.getTarifHoraire() != null ? lmReq.getTarifHoraire()
                        : (md != null && md.getPrix() != null ? md.getPrix().intValue() : 0);

                LigneFacturationMainDoeuvre lfm = LigneFacturationMainDoeuvre.builder()
                        .facturation(avoirTTC)
                        .mainDoeuvre(md)
                        .nbreHeure(nbreHeure)
                        .tarifHoraire(tarifHoraire)
                        .build();

                lignesMainDoeuvre.add(lfm);
                montantHT = montantHT.add(BigDecimal.valueOf((long) nbreHeure * tarifHoraire));
            }
        }

        BigDecimal montantTVA = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(request.getAppliquerTVA())) {
            montantTVA = montantHT.multiply(BigDecimal.valueOf(0.18));
        }

        BigDecimal montantTimbre = request.getMontantTimbre() != null ? request.getMontantTimbre() : BigDecimal.ZERO;
        BigDecimal montantTTC = montantHT.add(montantTVA).add(montantTimbre);

        avoirTTC.setMontantHT(montantHT);
        avoirTTC.setMontantTVA(montantTVA);
        avoirTTC.setMontantTimbre(montantTimbre);
        avoirTTC.setMontantTTC(montantTTC);
        avoirTTC.setMontantTotal(montantTTC);
        avoirTTC.setLignesFacturationPieces(lignesPieces);
        avoirTTC.setLignesFacturationMainDoeuvres(lignesMainDoeuvre);

        AvoirTTC saved = avoirTTCRepository.save(avoirTTC);
        return mapToResponse(saved);
    }

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
                document.add(new Paragraph("Agent : " + a.getAgent().getFirstName() + " " + a.getAgent().getLastName(),
                        fontTexte));
            }

            Client client = a.getClient();
            if (client == null && a.getOrdreReparation() != null && a.getOrdreReparation().getVehicule() != null) {
                client = a.getOrdreReparation().getVehicule().getClient();
            }

            Vehicule v = a.getVehicule();
            if (v == null && a.getOrdreReparation() != null) {
                v = a.getOrdreReparation().getVehicule();
            }

            if (client != null) {
                document.add(
                        new Paragraph("Client : " + client.getFirstName() + " " + client.getLastName(), fontTexte));
            }
            if (v != null) {
                document.add(new Paragraph("Véhicule : " + (v.getMarque() != null ? v.getMarque() : "") + " "
                        + (v.getModele() != null ? v.getModele() : "") + " (Immat: " + v.getImmatriculation() + ")",
                        fontTexte));
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
                tablePieces.setWidths(new float[] { 4f, 2f, 2f, 2f });

                String[] headersPieces = { "Désignation", "Quantité", "Prix Unitaire", "Total" };
                for (String header : headersPieces) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
                    cell.setBackgroundColor(Color.DARK_GRAY);
                    cell.setPadding(5);
                    tablePieces.addCell(cell);
                }

                for (LigneFacturationPiece ligne : a.getLignesFacturationPieces()) {
                    String ref = ligne.getPiece() != null ? ligne.getPiece().getDesignation()
                            : (ligne.getDesignationPds() != null ? ligne.getDesignationPds() : "N/A");
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
                tableMo.setWidths(new float[] { 4f, 2f, 2f, 2f });

                String[] headersMo = { "Catégorie", "Heures", "Tarif Horaire", "Total" };
                for (String header : headersMo) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
                    cell.setBackgroundColor(Color.DARK_GRAY);
                    cell.setPadding(5);
                    tableMo.addCell(cell);
                }

                for (LigneFacturationMainDoeuvre ligne : a.getLignesFacturationMainDoeuvres()) {
                    String cat = ligne.getMainDoeuvre() != null && ligne.getMainDoeuvre().getCategorie() != null
                            ? ligne.getMainDoeuvre().getCategorie().getNom()
                            : "N/A";
                    tableMo.addCell(new Phrase(cat, fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(ligne.getNbreHeure()), fontTexte));
                    tableMo.addCell(new Phrase(String.valueOf(ligne.getTarifHoraire()), fontTexte));
                    tableMo.addCell(
                            new Phrase(String.valueOf(ligne.getNbreHeure() * ligne.getTarifHoraire()), fontTexte));
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
        Client client = a.getClient();
        if (client == null && a.getOrdreReparation() != null && a.getOrdreReparation().getVehicule() != null) {
            client = a.getOrdreReparation().getVehicule().getClient();
        }

        Vehicule vehicule = a.getVehicule();
        if (vehicule == null && a.getOrdreReparation() != null) {
            vehicule = a.getOrdreReparation().getVehicule();
        }

        return AvoirTTCResponse.builder()
                .id(a.getId())
                .numero(a.getNumero())
                .dateCreation(a.getDateCreation())
                .dateModification(a.getDateModification())
                .montantHT(a.getMontantHT())
                .montantTVA(a.getMontantTVA())
                .montantTTC(a.getMontantTTC())
                .montantTimbre(a.getMontantTimbre())
                .montantTotal(a.getMontantTTC())
                .agentId(a.getAgent() != null ? a.getAgent().getId() : null)
                .agentNom(a.getAgent() != null ? a.getAgent().getFirstName() + " " + a.getAgent().getLastName() : null)
                .remarque(a.getRemarque())
                .kilometrage(a.getKilometrage())
                .clientId(client != null ? client.getId() : null)
                .clientNom(client != null ? client.getFirstName() + " " + client.getLastName() : null)
                .vehiculeId(vehicule != null ? vehicule.getId() : null)
                .immatriculation(vehicule != null ? vehicule.getImmatriculation() : null)
                .numeroChassis(vehicule != null ? vehicule.getNumeroChassis() : null)
                .marque(vehicule != null ? vehicule.getMarque() : null)
                .modele(vehicule != null ? vehicule.getModele() : null)
                .annee(vehicule != null ? vehicule.getAnnee() : null)
                .numeroBonDeCommande(a.getBonDeCommande() != null ? a.getBonDeCommande().getNumero() : null)
                .lignesPieces(a.getLignesFacturationPieces() == null ? List.of()
                        : a.getLignesFacturationPieces().stream()
                                .map(lp -> LigneFacturationPieceResponse.builder()
                                        .id(lp.getId())
                                        .pieceId(lp.getPiece() != null ? lp.getPiece().getId() : null)
                                        .designationPiece(lp.getPiece() != null ? lp.getPiece().getDesignation()
                                                : lp.getDesignationPds())
                                        .quantite(lp.getQuantite())
                                        .prix(lp.getPrix())
                                        .montantTotal(lp.getQuantite() * lp.getPrix())
                                        .build())
                                .collect(Collectors.toList()))
                .lignesMainDoeuvres(a.getLignesFacturationMainDoeuvres() == null ? List.of()
                        : a.getLignesFacturationMainDoeuvres().stream()
                                .map(lm -> LigneFacturationMainDoeuvreResponse.builder()
                                        .id(lm.getId())
                                        .mainDoeuvreId(lm.getMainDoeuvre() != null ? lm.getMainDoeuvre().getId() : null)
                                        .descriptionMainDoeuvre(lm.getMainDoeuvre() != null
                                                && lm.getMainDoeuvre().getCategorie() != null
                                                        ? lm.getMainDoeuvre().getCategorie().getNom()
                                                        : null)
                                        .nbreHeure(lm.getNbreHeure())
                                        .tarifHoraire(lm.getTarifHoraire())
                                        .montantTotal(lm.getNbreHeure() * lm.getTarifHoraire())
                                        .build())
                                .collect(Collectors.toList()))
                .build();
    }
}
