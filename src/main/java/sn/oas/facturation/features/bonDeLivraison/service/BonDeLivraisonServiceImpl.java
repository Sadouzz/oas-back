package sn.oas.facturation.features.bonDeLivraison.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
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
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.bonDeLivraison.data.entity.BonDeLivraison;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonCreateRequest;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonFactureRequest;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonListRow;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonOrdreContextResponse;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonResponse;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonSortieReference;
import sn.oas.facturation.features.bonDeLivraison.repository.BonDeLivraisonRepository;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.bonDeSortie.data.entity.LigneBonDeSortieMainDoeuvre;
import sn.oas.facturation.features.bonDeSortie.data.entity.LigneBonDeSortiePiece;
import sn.oas.facturation.features.bonDeSortie.data.enums.StatutBon;
import sn.oas.facturation.features.bonDeSortie.dto.BonDeSortieDisponibleResponse;
import sn.oas.facturation.features.bonDeSortie.repository.BonDeSortieRepository;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.facturation.data.entity.Facturation;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationMainDoeuvre;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationPiece;
import sn.oas.facturation.features.facturation.data.enums.StatutFacturation;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.facture.service.FacturePaymentMode;
import sn.oas.facturation.features.fiscal.service.FiscalConfigurationService;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.features.noteDePrix.repository.NoteDePrixRepository;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.ordreTravail.data.entity.OrdreTravail;
import sn.oas.facturation.features.pdfGenerator.service.OasBrandedPdfPageEvent;
import sn.oas.facturation.features.pdfGenerator.service.PdfLayout;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.data.entity.PieceMouvement;
import sn.oas.facturation.features.piecedetache.data.enums.TypeMouvement;
import sn.oas.facturation.features.piecedetache.data.enums.TypePiece;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.features.piecedetache.repository.PieceMouvementRepository;
import sn.oas.facturation.features.proforma.data.entity.Proforma;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.user.repository.UserRepository;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.shared.OrdreAffichageLignes;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.documentNumber.DocumentType;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BonDeLivraisonServiceImpl implements BonDeLivraisonService {

    private final BonDeLivraisonRepository repository;
    private final BonDeSortieRepository bsRepository;
    private final OrdreReparationRepository orRepository;
    private final FactureRepository factureRepository;
    private final NoteDePrixRepository noteRepository;
    private final UserRepository userRepository;
    private final DocumentNumberGeneratorService numberGenerator;
    private final FiscalConfigurationService fiscalService;
    private final PieceDetacheRepository pieceRepository;
    private final PieceMouvementRepository mouvementRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BonDeLivraisonResponse> list(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by("id").descending());
        Page<BonDeLivraisonListRow> results = keyword == null || keyword.isBlank()
                ? repository.findListRows(pageable)
                : repository.searchListRows(keyword.trim(), pageable);
        List<Long> bonIds = results.getContent().stream().map(BonDeLivraisonListRow::id).toList();
        Map<Long, List<BonDeLivraisonSortieReference>> references = bonIds.isEmpty()
                ? Map.of()
                : repository.findSortieReferences(bonIds).stream()
                        .collect(Collectors.groupingBy(BonDeLivraisonSortieReference::bonDeLivraisonId));
        return results.map(row -> BonDeLivraisonResponse.fromListRow(row, references.get(row.id())));
    }

    @Override
    @Transactional(readOnly = true)
    public BonDeLivraisonResponse get(Long id) {
        return BonDeLivraisonResponse.from(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id) {
        return generatePdf(id, PdfLayout.AVEC_ENTETE);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id, String layoutKey) {
        boolean branded = PdfLayout.AVEC_ENTETE.equals(PdfLayout.normalize(layoutKey));
        BonDeLivraison bl = find(id);
        var output = new ByteArrayOutputStream();
        var doc = branded
                ? new Document(PageSize.A4, 36, 36, 105, 78)
                : new Document(PageSize.A4, 36, 36, 44, 44);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, output);
            if (branded) {
                writer.setPageEvent(new OasBrandedPdfPageEvent());
            }
            doc.open();
            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17, new Color(24, 43, 68));
            Font body = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font head = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Paragraph p = new Paragraph("BON DE LIVRAISON", title);
            p.setSpacingAfter(12);
            doc.add(p);
            doc.add(new Paragraph("N° : " + bl.getNumero(), body));
            doc.add(new Paragraph("Date : " + bl.getDateCreation(), body));
            doc.add(new Paragraph("Ordre de réparation : " + (bl.getOrdreReparation() != null ? bl.getOrdreReparation().getNumero() : "-"), body));
            doc.add(new Paragraph("Client : " + (bl.getClient() != null ? (bl.getClient().getFirstName() + " " + bl.getClient().getLastName()) : "-"), body));
            doc.add(new Paragraph("Véhicule : " + (bl.getVehicule() != null ? bl.getVehicule().getImmatriculation() : "-"), body));
            doc.add(new Paragraph("Bons de sortie : " + (bl.getBonsDeSortie() != null ? bl.getBonsDeSortie().stream().map(BonDeSortie::getReference).collect(Collectors.joining(", ")) : "-"), body));
            if (bl.getRemarque() != null && !bl.getRemarque().isBlank()) {
                doc.add(new Paragraph("Remarque : " + bl.getRemarque(), body));
            }
            doc.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(new float[]{5f, 1f, 1.5f, 1.7f});
            table.setWidthPercentage(100);
            for (String label : List.of("Désignation", "Qté", "Prix unitaire", "Montant")) {
                PdfPCell cell = new PdfPCell(new Phrase(label, head));
                cell.setBackgroundColor(new Color(38, 58, 82));
                cell.setPadding(7);
                table.addCell(cell);
            }
            for (var l : OrdreAffichageLignes.plusRecentesEnPremier(bl.getLignesFacturationPieces(), LigneFacturationPiece::getId)) {
                PdfPCell designationCell = new PdfPCell(new Phrase(l.getDesignationPds() != null ? l.getDesignationPds() : l.getPiece() == null ? "Pièce" : l.getPiece().getDesignation(), body));
                var datesSaisie = l.getDatesHeuresSaisieSource();
                if (datesSaisie != null && !datesSaisie.isEmpty()) {
                    String datesFormatees = datesSaisie.stream().map(d -> d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                            .collect(Collectors.joining(", "));
                    designationCell.addElement(new Paragraph("Saisies : " + datesFormatees, FontFactory.getFont(FontFactory.HELVETICA, 7, Color.DARK_GRAY)));
                }
                table.addCell(designationCell);
                table.addCell(new Phrase(String.valueOf(l.getQuantite()), body));
                table.addCell(new Phrase(String.valueOf(l.getPrix()), body));
                table.addCell(new Phrase(net(BigDecimal.valueOf((long) l.getPrix() * l.getQuantite()), nz(l.getTauxRemise())).toPlainString(), body));
            }
            for (var l : bl.getLignesFacturationMainDoeuvres()) {
                table.addCell(new Phrase(l.getMainDoeuvre().getDescription(), body));
                table.addCell(new Phrase(String.valueOf(l.getNbreHeure()), body));
                table.addCell(new Phrase(String.valueOf(l.getTarifHoraire()), body));
                table.addCell(new Phrase(net(BigDecimal.valueOf((long) l.getTarifHoraire() * l.getNbreHeure()), nz(l.getTauxRemise())).toPlainString(), body));
            }
            doc.add(table);
            doc.add(new Paragraph(" "));
            if (Boolean.TRUE.equals(bl.getTvaApplicable())) {
                doc.add(new Paragraph("Total HT : " + bl.getMontantHT(), body));
            }
            doc.add(new Paragraph("TVA : " + (Boolean.TRUE.equals(bl.getTvaApplicable()) ? bl.getMontantTVA() : "Désactivée"), body));
            String totalLabel = Boolean.TRUE.equals(bl.getTvaApplicable()) ? "Total TTC" : "Total HT";
            Paragraph total = new Paragraph(totalLabel + " : " + bl.getMontantTTC(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            total.setAlignment(Element.ALIGN_RIGHT);
            doc.add(total);
            doc.close();
            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de générer le PDF du bon de livraison.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BonDeSortieDisponibleResponse> bonsDeSortieDisponibles(Long ordreId) {
        orRepository.findById(ordreId).orElseThrow(() -> new ResourceNotFoundException("Ordre de réparation introuvable."));
        return bsRepository.trouverDisponiblesParOrdreEtStatut(ordreId, StatutBon.VALIDE);
    }

    @Override
    @Transactional(readOnly = true)
    public BonDeLivraisonOrdreContextResponse contexteOrdre(Long ordreId) {
        OrdreReparation ordre = orRepository.findById(ordreId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de réparation introuvable."));
        var bons = bsRepository.trouverDisponiblesParOrdreEtStatut(ordreId, StatutBon.VALIDE);
        var bon = repository.findByOrdreReparationId(ordreId).orElse(null);
        var summary = bon == null ? null : new BonDeLivraisonOrdreContextResponse.BonDeLivraisonSummary(
                bon.getId(), bon.getNumero(), bon.getMontantTTC(), bon.getTvaApplicable(),
                bon.getFacture() == null ? null : bon.getFacture().getId(),
                bon.getFacture() == null ? null : bon.getFacture().getNumero(),
                bon.getNoteDePrix() == null ? null : bon.getNoteDePrix().getId(),
                bon.getNoteDePrix() == null ? null : bon.getNoteDePrix().getNumero());
        return new BonDeLivraisonOrdreContextResponse(ordre.getId(), ordre.getNumero(), tvaParDefaut(ordre), bons, summary);
    }

    @Override
    @Transactional
    public BonDeLivraisonResponse create(BonDeLivraisonCreateRequest req) {
        OrdreReparation ordre = orRepository.findByIdForFacturation(req.getOrdreReparationId())
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de réparation introuvable."));
        if (repository.existsByOrdreReparationId(ordre.getId())) {
            throw new BadRequestException("Un bon de livraison existe déjà pour cet ordre de réparation.");
        }
        if (factureRepository.findFirstByOrdreReparationId(ordre.getId()).isPresent()) {
            throw new BadRequestException("Une facture existe déjà pour cet ordre. Le bon de livraison doit être créé avant la facture.");
        }
        var bons = bsRepository.findByOrdreReparationIdAndStatutOrderByDateDesc(ordre.getId(), StatutBon.VALIDE);
        if (bons.isEmpty()) {
            throw new BadRequestException("Aucun bon de sortie validé n'est lié à cet ordre de réparation.");
        }
        Client client = ordre.getClient() != null ? ordre.getClient() : ordre.getVehicule() == null ? null : ordre.getVehicule().getClient();
        if (client == null || ordre.getVehicule() == null) {
            throw new BadRequestException("L'ordre doit avoir un client et un véhicule.");
        }
        for (BonDeSortie bs : bons) {
            if (bs.getStatut() != StatutBon.VALIDE) {
                throw new BadRequestException("Seuls les bons de sortie validés peuvent être livrés.");
            }
            if (bs.getOrdreReparation() == null || !ordre.getId().equals(bs.getOrdreReparation().getId())) {
                throw new BadRequestException("Chaque bon de sortie doit être lié à l'ordre de réparation sélectionné.");
            }
            if (bs.getClient() == null || !client.getId().equals(bs.getClient().getId()) || bs.getVehicule() == null || !ordre.getVehicule().getId().equals(bs.getVehicule().getId())) {
                throw new BadRequestException("Le client ou le véhicule d'un bon de sortie ne correspond pas à l'ordre.");
            }
        }
        Agent agent = currentAgent();
        boolean tva = req.getAppliquerTVA() == null ? tvaParDefaut(ordre) : Boolean.TRUE.equals(req.getAppliquerTVA());
        BonDeLivraison bl = BonDeLivraison.builder()
                .numero(numberGenerator.generateNextNumber(DocumentType.BL))
                .garage(ordre.getGarage())
                .client(client)
                .vehicule(ordre.getVehicule())
                .ordreReparation(ordre)
                .agent(agent)
                .kilometrage(0d)
                .remarque(req.getRemarque())
                .statut(StatutFacturation.EN_ATTENTE)
                .bonsDeSortie(new ArrayList<>(bons))
                .lignesFacturationPieces(new ArrayList<>())
                .lignesFacturationMainDoeuvres(new ArrayList<>())
                .tvaApplicable(tva)
                .build();

        BigDecimal ht = BigDecimal.ZERO;
        Map<PdgAggregationKey, LigneFacturationPiece> lignesPdgConsolidees = new LinkedHashMap<>();
        Map<PdsAggregationKey, LigneFacturationPiece> lignesPdsConsolidees = new LinkedHashMap<>();

        for (BonDeSortie bs : bons) {
            for (LigneBonDeSortiePiece l : bs.getLignesBonDeSortiePieces()) {
                if (l.getQuantite() == null || l.getQuantite() <= 0) continue;
                BigDecimal remise = nz(l.getTauxRemise());
                boolean isPdg = l.getPiece() != null && l.getPiece().getType() == TypePiece.PDG;
                String effectiveDesignation = l.getDesignation() != null && !l.getDesignation().isBlank()
                        ? l.getDesignation()
                        : (l.getPiece() != null ? l.getPiece().getDesignation() : "");
                if (isPdg) {
                    PdgAggregationKey key = new PdgAggregationKey(l.getPiece().getId(), l.getPrix(), remise.stripTrailingZeros());
                    LigneFacturationPiece row = lignesPdgConsolidees.get(key);
                    if (row == null) {
                        row = nouvelleLigneFacturation(bl, l.getPiece(), effectiveDesignation, l.getQuantite(), l.getPrix(), remise);
                        row.setDateHeureSaisie(l.getDateHeureSaisie());
                        lignesPdgConsolidees.put(key, row);
                        bl.getLignesFacturationPieces().add(row);
                    } else {
                        row.setQuantite(row.getQuantite() + l.getQuantite());
                    }
                    if (l.getDateHeureSaisie() != null) row.getDatesHeuresSaisieSource().add(l.getDateHeureSaisie());
                } else if (l.getPiece() == null && effectiveDesignation != null && !effectiveDesignation.isBlank()) {
                    PdsAggregationKey key = new PdsAggregationKey(normaliserDesignation(effectiveDesignation), l.getPrix(), remise.stripTrailingZeros());
                    LigneFacturationPiece row = lignesPdsConsolidees.get(key);
                    if (row == null) {
                        row = nouvelleLigneFacturation(bl, null, effectiveDesignation, l.getQuantite(), l.getPrix(), remise);
                        row.setDateHeureSaisie(l.getDateHeureSaisie());
                        lignesPdsConsolidees.put(key, row);
                        bl.getLignesFacturationPieces().add(row);
                    } else {
                        row.setQuantite(row.getQuantite() + l.getQuantite());
                    }
                    if (l.getDateHeureSaisie() != null) row.getDatesHeuresSaisieSource().add(l.getDateHeureSaisie());
                } else {
                    LigneFacturationPiece row = nouvelleLigneFacturation(bl, l.getPiece(), effectiveDesignation, l.getQuantite(), l.getPrix(), remise);
                    row.setDateHeureSaisie(l.getDateHeureSaisie());
                    if (l.getDateHeureSaisie() != null) row.getDatesHeuresSaisieSource().add(l.getDateHeureSaisie());
                    bl.getLignesFacturationPieces().add(row);
                }
            }
            for (LigneBonDeSortieMainDoeuvre l : bs.getLignesBonDeSortieMainDoeuvres()) {
                if (l.getMainDoeuvre() == null || l.getNbreHeure() == null || l.getNbreHeure() <= 0) continue;
                var row = LigneFacturationMainDoeuvre.builder()
                        .facturation(bl)
                        .mainDoeuvre(l.getMainDoeuvre())
                        .nbreHeure(l.getNbreHeure())
                        .tarifHoraire(l.getTarifHoraire())
                        .tauxRemise(nz(l.getTauxRemise()))
                        .dateHeureSaisie(l.getDateHeureSaisie())
                        .build();
                bl.getLignesFacturationMainDoeuvres().add(row);
            }
        }

        for (LigneFacturationPiece ligne : lignesPdgConsolidees.values()) {
            ligne.getDatesHeuresSaisieSource().sort(Comparator.naturalOrder());
        }
        for (LigneFacturationPiece ligne : lignesPdsConsolidees.values()) {
            ligne.getDatesHeuresSaisieSource().sort(Comparator.naturalOrder());
        }
        for (LigneFacturationPiece ligne : bl.getLignesFacturationPieces()) {
            ht = ht.add(net(BigDecimal.valueOf((long) ligne.getPrix() * ligne.getQuantite()), nz(ligne.getTauxRemise())));
        }
        for (LigneFacturationMainDoeuvre ligne : bl.getLignesFacturationMainDoeuvres()) {
            ht = ht.add(net(BigDecimal.valueOf((long) ligne.getTarifHoraire() * ligne.getNbreHeure()), nz(ligne.getTauxRemise())));
        }

        var fiscal = fiscalService.getCurrent();
        BigDecimal montantTVA = tva ? fiscalService.calculateRate(ht, fiscal.getTvaRate()) : BigDecimal.ZERO;
        bl.setMontantHT(ht);
        bl.setMontantTVA(montantTVA);
        bl.setMontantTimbre(BigDecimal.ZERO);
        bl.setMontantTTC(ht.add(montantTVA));
        bl.setMontantTotal(ht.add(montantTVA));

        if (ordre.getStatut() == StatutOrdreReparation.ORDRE_DE_TRAVAIL || ordre.getStatut() == StatutOrdreReparation.PROFORMA) {
            ordre.setStatut(StatutOrdreReparation.BL);
            orRepository.save(ordre);
        }
        return BonDeLivraisonResponse.from(repository.save(bl));
    }

    /**
     * Crée ou met à jour le Bon de Livraison directement à partir d'un Bon de Sortie.
     * Toutes les pièces et main-d'œuvre du BS sont transférées vers le BL avec leurs remises respectives.
     */
    @Override
    @Transactional
    public BonDeLivraison creerOuMettreAJourDepuisBonDeSortie(BonDeSortie bs, OrdreTravail ot) {
        if (bs == null) {
            return null;
        }
        OrdreReparation ordre = ot != null && ot.getOrdreReparation() != null
                ? ot.getOrdreReparation()
                : bs.getOrdreReparation();
        if (ordre == null) {
            log.warn("Impossible de créer le bon de livraison : aucun ordre de réparation trouvé pour le BS id={}", bs.getId());
            return null;
        }

        Client client = ot != null && ot.getClient() != null ? ot.getClient()
                : (ordre.getClient() != null ? ordre.getClient() : bs.getClient());
        Vehicule vehicule = ot != null && ot.getVehicule() != null ? ot.getVehicule()
                : (ordre.getVehicule() != null ? ordre.getVehicule() : bs.getVehicule());
        Garage garage = ot != null && ot.getGarage() != null ? ot.getGarage()
                : (ordre.getGarage() != null ? ordre.getGarage() : bs.getGarage());

        Agent agent = currentAgent();
        if (agent == null && bs != null) {
            agent = bs.getAgentEmetteur();
        }

        boolean tva;
        Proforma proforma = ot != null && ot.getProforma() != null ? ot.getProforma()
                : (ordre.getProforma() != null ? ordre.getProforma() : bs.getProforma());
        if (proforma != null && proforma.getTvaApplicable() != null) {
            tva = proforma.getTvaApplicable();
        } else if (proforma != null && proforma.getMontantTVA() != null) {
            tva = proforma.getMontantTVA().signum() > 0;
        } else {
            tva = tvaParDefaut(ordre);
        }

        Double kilometrage = ot != null && ot.getKilometrageSortie() != null ? ot.getKilometrageSortie()
                : (vehicule != null && vehicule.getKilometrage() != null ? vehicule.getKilometrage().doubleValue() : 0.0);

        String remarque = ot != null && ot.getRemarquesPreparation() != null && !ot.getRemarquesPreparation().isBlank()
                ? ot.getRemarquesPreparation() : bs.getRemarque();

        Optional<BonDeLivraison> existantOpt = (ordre != null)
                ? repository.findByOrdreReparationId(ordre.getId())
                : (ot != null ? repository.findByOrdreTravailId(ot.getId()) : Optional.empty());
        BonDeLivraison bl;
        if (existantOpt.isPresent()) {
            bl = existantOpt.get();
            if (bl.getOrdreReparation() == null && ordre != null) {
                bl.setOrdreReparation(ordre);
            }
            if (bl.getOrdreTravail() == null && ot != null) {
                bl.setOrdreTravail(ot);
            }
            if (bl.getFacture() != null) {
                log.info("Le bon de livraison {} a déjà une facture générée, aucune modification effectuée.", bl.getNumero());
                return bl;
            }
            if (remarque != null && !remarque.isBlank()) {
                bl.setRemarque(remarque);
            }
            bl.getLignesFacturationPieces().clear();
            bl.getLignesFacturationMainDoeuvres().clear();
        } else {
            bl = BonDeLivraison.builder()
                    .numero(numberGenerator.generateNextNumber(DocumentType.BL))
                    .ordreReparation(ordre)
                    .ordreTravail(ot)
                    .client(client)
                    .vehicule(vehicule)
                    .garage(garage)
                    .agent(agent)
                    .kilometrage(kilometrage)
                    .remarque(remarque)
                    .statut(StatutFacturation.EN_ATTENTE)
                    .tvaApplicable(tva)
                    .bonsDeSortie(new ArrayList<>())
                    .lignesFacturationPieces(new ArrayList<>())
                    .lignesFacturationMainDoeuvres(new ArrayList<>())
                    .build();
        }

        if (ot != null) {
            bl.setOrdreTravail(ot);
        } else if (bs.getOrdreTravail() != null) {
            bl.setOrdreTravail(bs.getOrdreTravail());
        }

        if (bl.getBonsDeSortie() == null) {
            bl.setBonsDeSortie(new ArrayList<>());
        }
        if (!bl.getBonsDeSortie().contains(bs)) {
            bl.getBonsDeSortie().add(bs);
        }

        if (bs.getLignesBonDeSortiePieces() != null) {
            for (LigneBonDeSortiePiece lp : bs.getLignesBonDeSortiePieces()) {
                if (lp.getQuantite() == null || lp.getQuantite() <= 0) continue;
                BigDecimal remise = nz(lp.getTauxRemise());
                int prix = lp.getPrix() != null ? lp.getPrix() : 0;
                int qte = lp.getQuantite();
                String designation = lp.getDesignation() != null ? lp.getDesignation()
                        : (lp.getPiece() != null ? lp.getPiece().getDesignation() : "");

                List<LocalDateTime> datesSaisie = new ArrayList<>();
                if (lp.getDateHeureSaisie() != null) {
                    datesSaisie.add(lp.getDateHeureSaisie());
                }

                LigneFacturationPiece lignePiece = LigneFacturationPiece.builder()
                        .facturation(bl)
                        .piece(lp.getPiece())
                        .isCustom(lp.getPiece() == null)
                        .designationPds(designation)
                        .quantite(qte)
                        .prix(prix)
                        .tauxRemise(remise)
                        .dateHeureSaisie(lp.getDateHeureSaisie())
                        .datesHeuresSaisieSource(datesSaisie)
                        .build();
                bl.getLignesFacturationPieces().add(lignePiece);
            }
        }

        if (bs.getLignesBonDeSortieMainDoeuvres() != null) {
            for (LigneBonDeSortieMainDoeuvre lmo : bs.getLignesBonDeSortieMainDoeuvres()) {
                if (lmo.getMainDoeuvre() == null || lmo.getNbreHeure() == null || lmo.getNbreHeure() <= 0) continue;
                BigDecimal remise = nz(lmo.getTauxRemise());
                int tarif = lmo.getTarifHoraire() != null ? lmo.getTarifHoraire() : 0;
                int heures = lmo.getNbreHeure();

                LigneFacturationMainDoeuvre ligneMo = LigneFacturationMainDoeuvre.builder()
                        .facturation(bl)
                        .mainDoeuvre(lmo.getMainDoeuvre())
                        .nbreHeure(heures)
                        .tarifHoraire(tarif)
                        .tauxRemise(remise)
                        .dateHeureSaisie(lmo.getDateHeureSaisie())
                        .build();
                bl.getLignesFacturationMainDoeuvres().add(ligneMo);
            }
        }

        BigDecimal ht = BigDecimal.ZERO;
        for (LigneFacturationPiece l : bl.getLignesFacturationPieces()) {
            ht = ht.add(net(BigDecimal.valueOf((long) l.getPrix() * l.getQuantite()), nz(l.getTauxRemise())));
        }
        for (LigneFacturationMainDoeuvre l : bl.getLignesFacturationMainDoeuvres()) {
            ht = ht.add(net(BigDecimal.valueOf((long) l.getTarifHoraire() * l.getNbreHeure()), nz(l.getTauxRemise())));
        }

        var fiscal = fiscalService.getCurrent();
        BigDecimal montantTVA = Boolean.TRUE.equals(bl.getTvaApplicable())
                ? fiscalService.calculateRate(ht, fiscal.getTvaRate())
                : BigDecimal.ZERO;

        bl.setMontantHT(ht);
        bl.setMontantTVA(montantTVA);
        bl.setMontantTimbre(BigDecimal.ZERO);
        bl.setMontantTTC(ht.add(montantTVA));
        bl.setMontantTotal(ht.add(montantTVA));

        BonDeLivraison saved = repository.save(bl);

        if (ordre.getStatut() == StatutOrdreReparation.ORDRE_DE_TRAVAIL
                || ordre.getStatut() == StatutOrdreReparation.PROFORMA) {
            ordre.setStatut(StatutOrdreReparation.BL);
            orRepository.save(ordre);
        }

        return saved;
    }

    @Override
    @Transactional
    public Facture creerFactureDepuisOrdre(Long ordreId, BonDeLivraisonFactureRequest request) {
        String modePaiement = FacturePaymentMode.normalize(request == null ? null : request.getModePaiement());
        OrdreReparation ordre = orRepository.findByIdForFacturation(ordreId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de réparation introuvable."));
        BonDeLivraison bl = repository.findByOrdreReparationId(ordre.getId()).orElse(null);
        Facture factureExistante = factureRepository.findFirstByOrdreReparationId(ordre.getId()).orElse(null);
        if (factureExistante != null) {
            if (bl == null || factureExistante.getBonDeLivraison() == null
                    || !bl.getId().equals(factureExistante.getBonDeLivraison().getId())) {
                throw new BadRequestException("Une facture existe déjà pour cet ordre, mais elle n'est pas issue de son bon de livraison.");
            }
            if (ordre.getStatut() == null || ordre.getStatut().getEtape() < StatutOrdreReparation.FACTURATION.getEtape()) {
                ordre.setStatut(StatutOrdreReparation.FACTURATION);
                ordre.setUpdatedAt(LocalDateTime.now());
                orRepository.save(ordre);
            }
            return factureExistante;
        }

        if (bl == null) {
            BonDeLivraisonCreateRequest createRequest = new BonDeLivraisonCreateRequest();
            createRequest.setOrdreReparationId(ordre.getId());
            createRequest.setAppliquerTVA(FacturePaymentMode.appliquerTva(modePaiement));
            if (request != null) {
                createRequest.setRemarque(request.getRemarque());
            }
            BonDeLivraisonResponse created = create(createRequest);
            bl = repository.findById(created.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Le bon de livraison créé est introuvable."));
        }
        return convertirEnFacture(bl.getId(), request);
    }

    @Override
    @Transactional
    public Facture convertirEnFacture(Long id) {
        return convertirEnFacture(id, null);
    }

    @Override
    @Transactional
    public Facture convertirEnFacture(Long id, BonDeLivraisonFactureRequest request) {
        BonDeLivraison bl = repository.findByIdForConversion(id).orElseThrow(() -> new ResourceNotFoundException("Bon de livraison introuvable."));

        OrdreReparation ordre = bl.getOrdreReparation();
        if (ordre == null && bl.getOrdreTravail() != null && bl.getOrdreTravail().getOrdreReparation() != null) {
            ordre = bl.getOrdreTravail().getOrdreReparation();
            bl.setOrdreReparation(ordre);
            repository.save(bl);
        }
        if (ordre == null && bl.getBonsDeSortie() != null) {
            for (BonDeSortie bs : bl.getBonsDeSortie()) {
                if (bs.getOrdreReparation() != null) {
                    ordre = bs.getOrdreReparation();
                    bl.setOrdreReparation(ordre);
                    repository.save(bl);
                    break;
                }
                if (bs.getOrdreTravail() != null && bs.getOrdreTravail().getOrdreReparation() != null) {
                    ordre = bs.getOrdreTravail().getOrdreReparation();
                    bl.setOrdreReparation(ordre);
                    repository.save(bl);
                    break;
                }
            }
        }

        if (bl.getFacture() != null) {
            if (ordre != null && (ordre.getStatut() == null || ordre.getStatut().getEtape() < StatutOrdreReparation.FACTURATION.getEtape())) {
                ordre.setStatut(StatutOrdreReparation.FACTURATION);
                ordre.setUpdatedAt(LocalDateTime.now());
                orRepository.save(ordre);
            }
            return bl.getFacture();
        }

        if (ordre != null && factureRepository.findFirstByOrdreReparationId(ordre.getId()).isPresent()) {
            throw new BadRequestException("Une facture existe déjà pour cet ordre de réparation.");
        }
        String modePaiement = FacturePaymentMode.normalize(request == null ? null : request.getModePaiement());
        boolean appliquerTVA = FacturePaymentMode.appliquerTva(modePaiement);
        BigDecimal tauxTimbre = ordre != null && ordre.getProforma() != null && ordre.getProforma().getTimbreFiscalRate() != null
                ? ordre.getProforma().getTimbreFiscalRate() : fiscalService.getCurrent().getTimbreFiscalRate();
        var configurationFiscale = fiscalService.getCurrent();
        BigDecimal tva = appliquerTVA ? fiscalService.calculateRate(bl.getMontantHT(), configurationFiscale.getTvaRate()) : BigDecimal.ZERO;
        BigDecimal timbre = FacturePaymentMode.isHt(modePaiement)
                ? fiscalService.calculateRate(bl.getMontantHT(), tauxTimbre) : BigDecimal.ZERO;
        BigDecimal montantTtc = bl.getMontantHT().add(tva);
        BigDecimal montantTotal = montantTtc.add(timbre);
        Facture facture = Facture.builder()
                .numero(numberGenerator.generateNextNumber(DocumentType.FC))
                .garage(bl.getGarage())
                .client(bl.getClient())
                .vehicule(bl.getVehicule())
                .ordreReparation(ordre)
                .bonDeLivraison(bl)
                .proforma(ordre == null ? null : ordre.getProforma())
                .timbreFiscalRate(tauxTimbre)
                .modePaiement(modePaiement)
                .agent(currentAgent())
                .kilometrage(bl.getKilometrage())
                .remarque(bl.getRemarque())
                .statut(StatutFacturation.EN_ATTENTE)
                .tvaApplicable(appliquerTVA)
                .montantHT(bl.getMontantHT())
                .montantTVA(tva)
                .montantTTC(montantTtc)
                .montantTotal(montantTotal)
                .montantTimbre(timbre)
                .montantPaye(BigDecimal.ZERO)
                .resteAPayer(montantTotal)
                .statutPaiement(StatutPaiement.NON_PAYE)
                .lignesFacturationPieces(new ArrayList<>())
                .lignesFacturationMainDoeuvres(new ArrayList<>())
                .build();
        if (bl.getClient() != null && bl.getClient().getEcheance() != null && bl.getClient().getEcheance() > 0) {
            facture.setDateEcheance(LocalDateTime.now().plusDays(bl.getClient().getEcheance()));
        }
        for (var row : copyPieces(bl, facture)) facture.getLignesFacturationPieces().add(row);
        for (var row : copyMo(bl, facture)) facture.getLignesFacturationMainDoeuvres().add(row);
        for (var l : facture.getLignesFacturationPieces()) {
            if (l.getPiece() instanceof PDP p) {
                double before = p.getQteReelle() == null ? p.getStockMagasin() + p.getStockAtelier() : p.getQteReelle();
                p.setQteReelle(Math.max(0, before - l.getQuantite()));
                pieceRepository.save(p);
                mouvementRepository.save(PieceMouvement.builder().type(TypeMouvement.SORTIE_REELLE).quantite((double) l.getQuantite())
                        .stockMagasinAvant(p.getStockMagasin()).stockAtelierAvant(p.getStockAtelier()).stockMagasinApres(p.getStockMagasin())
                        .stockAtelierApres(p.getStockAtelier()).stockReelApres(p.getQteReelle()).numDocument(facture.getNumero())
                        .typeDocument("Facture").numeroSerie(p.getReference())
                        .immatriculation(bl.getVehicule() != null ? bl.getVehicule().getImmatriculation() : "")
                        .motif("Facture " + facture.getNumero()).piece(p).agent(facture.getAgent()).garage(bl.getGarage())
                        .prenom(bl.getClient() != null ? bl.getClient().getFirstName() : "")
                        .nom(bl.getClient() != null ? bl.getClient().getLastName() : "").build());
            }
        }
        if (ordre != null) {
            if (ordre.getStatut() == null || ordre.getStatut().getEtape() < StatutOrdreReparation.FACTURATION.getEtape()) {
                ordre.setStatut(StatutOrdreReparation.FACTURATION);
                ordre.setUpdatedAt(LocalDateTime.now());
                orRepository.save(ordre);
            }
            if (ordre.getProforma() != null) {
                ordre.getProforma().setStatut(StatutFacturation.CONVERTIE);
            }
        }
        return factureRepository.save(facture);
    }

    @Override
    @Transactional
    public NoteDePrix convertirEnNoteDePrix(Long id) {
        return convertirEnNoteDePrix(id, null);
    }

    @Override
    @Transactional
    public NoteDePrix convertirEnNoteDePrix(Long id, BonDeLivraisonFactureRequest request) {
        BonDeLivraison bl = repository.findByIdForConversion(id).orElseThrow(() -> new ResourceNotFoundException("Bon de livraison introuvable."));

        OrdreReparation ordre = bl.getOrdreReparation();
        if (ordre == null && bl.getOrdreTravail() != null && bl.getOrdreTravail().getOrdreReparation() != null) {
            ordre = bl.getOrdreTravail().getOrdreReparation();
            bl.setOrdreReparation(ordre);
            repository.save(bl);
        }
        if (ordre == null && bl.getBonsDeSortie() != null) {
            for (BonDeSortie bs : bl.getBonsDeSortie()) {
                if (bs.getOrdreReparation() != null) {
                    ordre = bs.getOrdreReparation();
                    bl.setOrdreReparation(ordre);
                    repository.save(bl);
                    break;
                }
                if (bs.getOrdreTravail() != null && bs.getOrdreTravail().getOrdreReparation() != null) {
                    ordre = bs.getOrdreTravail().getOrdreReparation();
                    bl.setOrdreReparation(ordre);
                    repository.save(bl);
                    break;
                }
            }
        }

        if (bl.getNoteDePrix() != null) {
            if (ordre != null && (ordre.getStatut() == null || ordre.getStatut().getEtape() < StatutOrdreReparation.FACTURATION.getEtape())) {
                ordre.setStatut(StatutOrdreReparation.FACTURATION);
                ordre.setUpdatedAt(LocalDateTime.now());
                orRepository.save(ordre);
            }
            return bl.getNoteDePrix();
        }

        String modePaiement = FacturePaymentMode.normalize(request == null ? null : request.getModePaiement());
        boolean appliquerTVA = FacturePaymentMode.appliquerTva(modePaiement);
        BigDecimal tauxTimbre = ordre != null && ordre.getProforma() != null && ordre.getProforma().getTimbreFiscalRate() != null
                ? ordre.getProforma().getTimbreFiscalRate() : fiscalService.getCurrent().getTimbreFiscalRate();
        var configurationFiscale = fiscalService.getCurrent();
        BigDecimal tva = appliquerTVA ? fiscalService.calculateRate(bl.getMontantHT(), configurationFiscale.getTvaRate()) : BigDecimal.ZERO;
        BigDecimal timbre = FacturePaymentMode.isHt(modePaiement)
                ? fiscalService.calculateRate(bl.getMontantHT(), tauxTimbre) : BigDecimal.ZERO;
        BigDecimal montantTotal = bl.getMontantHT().add(tva).add(timbre);

        NoteDePrix note = NoteDePrix.builder()
                .numero(numberGenerator.generateNextNumber(DocumentType.NP))
                .garage(bl.getGarage())
                .client(bl.getClient())
                .vehicule(bl.getVehicule())
                .ordreReparation(ordre)
                .bonDeLivraison(bl)
                .agent(currentAgent())
                .kilometrage(bl.getKilometrage())
                .remarque(request != null && request.getRemarque() != null ? request.getRemarque() : bl.getRemarque())
                .statut(StatutFacturation.EN_ATTENTE)
                .tvaApplicable(appliquerTVA)
                .montantHT(bl.getMontantHT())
                .montantTVA(tva)
                .montantTotal(montantTotal)
                .montantPaye(BigDecimal.ZERO)
                .montantTimbrePaye(timbre)
                .resteAPayer(montantTotal)
                .statutPaiement(StatutPaiement.NON_PAYE)
                .modePaiement(modePaiement)
                .lignesFacturationPieces(new ArrayList<>())
                .lignesFacturationMainDoeuvres(new ArrayList<>())
                .build();
        for (var row : copyPieces(bl, note)) note.getLignesFacturationPieces().add(row);
        for (var row : copyMo(bl, note)) note.getLignesFacturationMainDoeuvres().add(row);

        if (ordre != null) {
            if (ordre.getStatut() == null || ordre.getStatut().getEtape() < StatutOrdreReparation.FACTURATION.getEtape()) {
                ordre.setStatut(StatutOrdreReparation.FACTURATION);
                ordre.setUpdatedAt(LocalDateTime.now());
                orRepository.save(ordre);
            }
            if (ordre.getProforma() != null) {
                ordre.getProforma().setStatut(StatutFacturation.CONVERTIE);
            }
        }
        return noteRepository.save(note);
    }

    private List<LigneFacturationPiece> copyPieces(BonDeLivraison source, Facturation target) {
        return source.getLignesFacturationPieces().stream().map(l -> {
            String designation = l.getDesignationPds();
            if ((designation == null || designation.isBlank()) && l.getPiece() != null) {
                designation = l.getPiece().getDesignation();
            }
            return LigneFacturationPiece.builder()
                    .piece(l.getPiece())
                    .facturation(target)
                    .isCustom(l.getIsCustom() != null ? l.getIsCustom() : (l.getPiece() == null))
                    .designationPds(designation)
                    .quantite(l.getQuantite())
                    .prix(l.getPrix())
                    .tauxRemise(nz(l.getTauxRemise()))
                    .dateHeureSaisie(l.getDateHeureSaisie())
                    .datesHeuresSaisieSource(new ArrayList<>(l.getDatesHeuresSaisieSource() == null ? List.of() : l.getDatesHeuresSaisieSource()))
                    .build();
        }).toList();
    }

    private LigneFacturationPiece nouvelleLigneFacturation(BonDeLivraison bl, PieceDetache piece,
            String designation, Integer quantite, Integer prix, BigDecimal remise) {
        String effectiveDesignation = designation != null && !designation.isBlank()
                ? designation
                : (piece != null ? piece.getDesignation() : "");
        return LigneFacturationPiece.builder()
                .facturation(bl)
                .piece(piece)
                .isCustom(piece == null)
                .designationPds(effectiveDesignation)
                .quantite(quantite)
                .prix(prix)
                .tauxRemise(remise)
                .datesHeuresSaisieSource(new ArrayList<>())
                .build();
    }

    private record PdgAggregationKey(Long pieceId, Integer prix, BigDecimal tauxRemise) {}
    private record PdsAggregationKey(String designation, Integer prix, BigDecimal tauxRemise) {}

    private String normaliserDesignation(String designation) {
        return designation == null ? "" : designation.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private List<LigneFacturationMainDoeuvre> copyMo(BonDeLivraison source, Facturation target) {
        return source.getLignesFacturationMainDoeuvres().stream().map(l -> LigneFacturationMainDoeuvre.builder()
                .mainDoeuvre(l.getMainDoeuvre())
                .facturation(target)
                .nbreHeure(l.getNbreHeure())
                .tarifHoraire(l.getTarifHoraire())
                .tauxRemise(nz(l.getTauxRemise()))
                .dateHeureSaisie(l.getDateHeureSaisie())
                .build()).toList();
    }

    private BonDeLivraison find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bon de livraison introuvable."));
    }

    private Agent currentAgent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        User user = userRepository.findByUsername(auth.getName()).or(() -> userRepository.findByEmail(auth.getName())).orElse(null);
        return user instanceof Agent a ? a : null;
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private boolean tvaParDefaut(OrdreReparation ordre) {
        Proforma proforma = ordre.getProforma();
        if (proforma == null) return true;
        if (proforma.getTvaApplicable() != null) return proforma.getTvaApplicable();
        return proforma.getMontantTVA() != null && proforma.getMontantTVA().signum() > 0;
    }

    private BigDecimal net(BigDecimal base, BigDecimal rate) {
        return rate.signum() <= 0 ? base : base.subtract(base.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
    }
}

