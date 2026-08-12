package sn.oas.facturation.facture.service;

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
import sn.oas.facturation.facturation.data.entity.LigneFacturationMainDoeuvre;
import sn.oas.facturation.facturation.data.entity.LigneFacturationPiece;
import sn.oas.facturation.facturation.dto.LigneFacturationMainDoeuvreResponse;
import sn.oas.facturation.facturation.dto.LigneFacturationPieceResponse;
import sn.oas.facturation.facture.data.entity.Facture;
import sn.oas.facturation.facture.dto.FactureResponse;
import sn.oas.facturation.facture.repository.FactureRepository;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.auth.data.entity.User;
import sn.oas.facturation.auth.repository.UserRepository;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.ficheAtelier.data.entity.LigneFicheAtelierMainDoeuvre;
import sn.oas.facturation.ficheAtelier.data.entity.LigneFicheAtelierPiece;
import sn.oas.facturation.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.facture.dto.FactureCreateRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import sn.oas.facturation.auth.data.enums.Role;
import sn.oas.facturation.notification.service.AgentNotificationService;

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
    private final FicheAtelierRepository ficheAtelierRepository;
    private final VehiculeRepository vehiculeRepository;
    private final UserRepository userRepository;
    private final AgentNotificationService agentNotificationService;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;

    @Override
    @Transactional
    public FactureResponse createFacture(FactureCreateRequest request) {
        Client client = (Client) userRepository.findById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable"));
        
        Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                .orElseThrow(() -> new IllegalArgumentException("Véhicule introuvable"));

        FicheAtelier ficheAtelier = ficheAtelierRepository.findById(request.getFicheAtelierId())
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
                .ficheAtelier(ficheAtelier)
                .agent(agent)
                .kilometrage(request.getKilometrage() != null ? request.getKilometrage() : 0.0)
                .remarque(request.getRemarque())
                .build();

        BigDecimal ht = BigDecimal.ZERO;
        
        for (LigneFicheAtelierPiece ligneFiche : ficheAtelier.getLignesFicheAtelierPieces()) {
            LigneFacturationPiece lfp = LigneFacturationPiece.builder()
                    .facturation(facture)
                    .piece(ligneFiche.getPiece())
                    .quantite(ligneFiche.getQuantite())
                    .prix(ligneFiche.getPrix())
                    .build();
            facture.getLignesFacturationPieces().add(lfp);
            ht = ht.add(BigDecimal.valueOf((long) ligneFiche.getQuantite() * ligneFiche.getPrix()));
        }

        for (LigneFicheAtelierMainDoeuvre ligneFiche : ficheAtelier.getLignesFicheAtelierMainDoeuvres()) {
            LigneFacturationMainDoeuvre lfm = LigneFacturationMainDoeuvre.builder()
                    .facturation(facture)
                    .mainDoeuvre(ligneFiche.getMainDoeuvre())
                    .nbreHeure(ligneFiche.getNbreHeure())
                    .tarifHoraire(ligneFiche.getPrix())
                    .build();
            facture.getLignesFacturationMainDoeuvres().add(lfm);
            ht = ht.add(BigDecimal.valueOf((long) ligneFiche.getNbreHeure() * ligneFiche.getPrix()));
        }

        Double tvaRate = request.getTvaRate() != null ? request.getTvaRate() : 18.0;

        BigDecimal rateBD = BigDecimal.valueOf(tvaRate)
                .divide(BigDecimal.valueOf(100));

        BigDecimal tva = ht.multiply(rateBD);

        BigDecimal timbre = request.getMontantTimbre() != null
                ? request.getMontantTimbre()
                : BigDecimal.valueOf(100);

        BigDecimal autre = request.getMontantAutre() != null
                ? request.getMontantAutre()
                : BigDecimal.ZERO;

        BigDecimal ttc = ht.add(tva);

        facture.setMontantHT(ht);
        facture.setMontantTVA(tva);
        facture.setMontantTTC(ttc);
        facture.setMontantTimbre(timbre);
        facture.setMontantAutre(autre);
        facture.setMontantTotal(ttc.add(timbre).add(autre));
        facture.setMontantPaye(BigDecimal.ZERO);
        facture.setResteAPayer(ttc.add(timbre).add(autre));
        facture.setStatutPaiement(sn.oas.facturation.facture.data.enums.StatutPaiement.NON_PAYE);

        facture = factureRepository.save(facture);

        // Update FicheAtelier status to EN_ATTENTE_PAIEMENT
        if (ficheAtelier != null && ficheAtelier.getStatut() != sn.oas.facturation.ficheAtelier.data.enums.StatutFiche.EN_ATTENTE_PAIEMENT) {
            ficheAtelier.setStatut(sn.oas.facturation.ficheAtelier.data.enums.StatutFiche.EN_ATTENTE_PAIEMENT);
            ficheAtelierRepository.save(ficheAtelier);
        }

        agentNotificationService.notifyRole(Role.AGENT, 
            "Nouvelle Facture", 
            "La facture " + facture.getNumero() + " a été générée et est en attente de paiement.");

        return mapToResponse(facture);
    }

    @Override
    @Transactional
    public FactureResponse createFactureAuto(FicheAtelier ficheAtelier) {
        Client client = null;
        Vehicule vehicule = ficheAtelier.getVehicule();
        if (vehicule != null) {
            client = vehicule.getClient();
        }

        String numero = documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.FC);

        Facture facture = Facture.builder()
                .numero(numero)
                .client(client)
                .vehicule(vehicule)
                .ficheAtelier(ficheAtelier)
                .agent(null)
                .kilometrage(vehicule != null && vehicule.getKilometrage() != null ? vehicule.getKilometrage() : 0.0)
                .remarque("Facture générée automatiquement depuis la Fiche Atelier " + ficheAtelier.getNumero())
                .build();

        BigDecimal ht = BigDecimal.ZERO;
        
        for (LigneFicheAtelierPiece ligneFiche : ficheAtelier.getLignesFicheAtelierPieces()) {
            LigneFacturationPiece lfp = LigneFacturationPiece.builder()
                    .facturation(facture)
                    .piece(ligneFiche.getPiece())
                    .quantite(ligneFiche.getQuantite())
                    .prix(ligneFiche.getPrix())
                    .build();
            facture.getLignesFacturationPieces().add(lfp);
            ht = ht.add(BigDecimal.valueOf((long) ligneFiche.getQuantite() * ligneFiche.getPrix()));
        }

        for (LigneFicheAtelierMainDoeuvre ligneFiche : ficheAtelier.getLignesFicheAtelierMainDoeuvres()) {
            LigneFacturationMainDoeuvre lfm = LigneFacturationMainDoeuvre.builder()
                    .facturation(facture)
                    .mainDoeuvre(ligneFiche.getMainDoeuvre())
                    .nbreHeure(ligneFiche.getNbreHeure())
                    .tarifHoraire(ligneFiche.getPrix())
                    .build();
            facture.getLignesFacturationMainDoeuvres().add(lfm);
            ht = ht.add(BigDecimal.valueOf((long) ligneFiche.getNbreHeure() * ligneFiche.getPrix()));
        }

        Double tvaRate = 18.0;

        BigDecimal rateBD = BigDecimal.valueOf(tvaRate)
                .divide(BigDecimal.valueOf(100));

        BigDecimal tva = ht.multiply(rateBD);

        BigDecimal timbre = BigDecimal.valueOf(100);
        BigDecimal autre = BigDecimal.ZERO;

        BigDecimal ttc = ht.add(tva);

        facture.setMontantHT(ht);
        facture.setMontantTVA(tva);
        facture.setMontantTTC(ttc);
        facture.setMontantTimbre(timbre);
        facture.setMontantAutre(autre);
        facture.setMontantTotal(ttc.add(timbre).add(autre));
        facture.setMontantPaye(BigDecimal.ZERO);
        facture.setResteAPayer(ttc.add(timbre).add(autre));
        facture.setStatutPaiement(sn.oas.facturation.facture.data.enums.StatutPaiement.NON_PAYE);

        facture = factureRepository.save(facture);

        // Update FicheAtelier status to EN_ATTENTE_PAIEMENT
        // REMOVED: Since the facture is generated automatically when the Bon de Sortie is validated, 
        // the repair hasn't even started yet! The Fiche Atelier must go to EN_ATTENTE_MECANICIEN (Step 7).
        // It will only transition to payment later when the repair is done.

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
                    String ref = ligne.getPiece() != null ? ligne.getPiece().getReference() : "N/A";
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
                .ficheAtelierId(f.getFicheAtelier() != null ? f.getFicheAtelier().getId() : null)
                .numeroFicheAtelier(f.getFicheAtelier() != null ? f.getFicheAtelier().getNumero() : null)
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
                .recus(f.getRecus() == null ? List.of() : f.getRecus().stream()
                        .map(r -> sn.oas.facturation.recu.dto.RecuResponse.builder()
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
