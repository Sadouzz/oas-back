package sn.oas.facturation.features.bonDeLivraison.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import sn.oas.facturation.features.bonDeLivraison.data.entity.BonDeLivraison;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonCreateRequest;
import sn.oas.facturation.features.bonDeLivraison.dto.BonDeLivraisonResponse;
import sn.oas.facturation.features.bonDeLivraison.repository.BonDeLivraisonRepository;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.bonDeSortie.data.entity.LigneBonDeSortieMainDoeuvre;
import sn.oas.facturation.features.bonDeSortie.data.entity.LigneBonDeSortiePiece;
import sn.oas.facturation.features.bonDeSortie.data.enums.StatutBon;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationMainDoeuvre;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationPiece;
import sn.oas.facturation.features.facturation.data.enums.StatutFacturation;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.features.noteDePrix.repository.NoteDePrixRepository;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.bonDeSortie.repository.BonDeSortieRepository;
import sn.oas.facturation.features.user.repository.UserRepository;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.data.entity.PieceMouvement;
import sn.oas.facturation.features.piecedetache.data.enums.TypeMouvement;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.features.piecedetache.repository.PieceMouvementRepository;
import sn.oas.facturation.features.main_doeuvre.repository.MainDoeuvreRepository;
import sn.oas.facturation.features.bonDeSortie.data.enums.TypeLigneBonDeSortie;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.documentNumber.DocumentType;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;
import sn.oas.facturation.features.fiscal.service.FiscalConfigurationService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.awt.Color;

@Service @RequiredArgsConstructor
public class BonDeLivraisonService {
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
    private final MainDoeuvreRepository mainDoeuvreRepository;

    @Transactional(readOnly = true)
    public Page<BonDeLivraisonResponse> list(int page, int size, String keyword) {
        var pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by("id").descending());
        Page<BonDeLivraison> results = keyword == null || keyword.isBlank()
                ? repository.findAll(pageable)
                : repository.search(keyword.trim(), pageable);
        return results
                .map(BonDeLivraisonResponse::from);
    }

    @Transactional(readOnly = true)
    public BonDeLivraisonResponse get(Long id) { return BonDeLivraisonResponse.from(find(id)); }

    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id) {
        BonDeLivraison bl = find(id);
        var output = new ByteArrayOutputStream();
        var doc = new Document(com.lowagie.text.PageSize.A4, 36, 36, 44, 44);
        try {
            PdfWriter.getInstance(doc, output); doc.open();
            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17, new Color(24, 43, 68));
            Font body = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font head = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Paragraph p = new Paragraph("BON DE LIVRAISON", title); p.setSpacingAfter(12); doc.add(p);
            doc.add(new Paragraph("N° : " + bl.getNumero(), body));
            doc.add(new Paragraph("Date : " + bl.getDateCreation(), body));
            doc.add(new Paragraph("Ordre de réparation : " + bl.getOrdreReparation().getNumero(), body));
            doc.add(new Paragraph("Client : " + bl.getClient().getFirstName() + " " + bl.getClient().getLastName(), body));
            doc.add(new Paragraph("Véhicule : " + bl.getVehicule().getImmatriculation(), body));
            doc.add(new Paragraph("Bons de sortie : " + bl.getBonsDeSortie().stream().map(BonDeSortie::getReference).collect(java.util.stream.Collectors.joining(", ")), body));
            if (bl.getRemarque() != null && !bl.getRemarque().isBlank()) doc.add(new Paragraph("Remarque : " + bl.getRemarque(), body));
            doc.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(new float[]{5f, 1f, 1.5f, 1.7f}); table.setWidthPercentage(100);
            for (String label : List.of("Désignation", "Qté", "Prix unitaire", "Montant")) {
                PdfPCell cell = new PdfPCell(new Phrase(label, head)); cell.setBackgroundColor(new Color(38, 58, 82)); cell.setPadding(7); table.addCell(cell);
            }
            for (var l : bl.getLignesFacturationPieces()) {
                table.addCell(new Phrase(l.getDesignationPds() != null ? l.getDesignationPds() : l.getPiece() == null ? "Pièce" : l.getPiece().getDesignation(), body));
                table.addCell(new Phrase(String.valueOf(l.getQuantite()), body)); table.addCell(new Phrase(String.valueOf(l.getPrix()), body));
                table.addCell(new Phrase(net(BigDecimal.valueOf((long) l.getPrix() * l.getQuantite()), nz(l.getTauxRemise())).toPlainString(), body));
            }
            for (var l : bl.getLignesFacturationMainDoeuvres()) {
                table.addCell(new Phrase(l.getMainDoeuvre().getDescription(), body)); table.addCell(new Phrase(String.valueOf(l.getNbreHeure()), body));
                table.addCell(new Phrase(String.valueOf(l.getTarifHoraire()), body));
                table.addCell(new Phrase(net(BigDecimal.valueOf((long) l.getTarifHoraire() * l.getNbreHeure()), nz(l.getTauxRemise())).toPlainString(), body));
            }
            doc.add(table); doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Total HT : " + bl.getMontantHT(), body));
            doc.add(new Paragraph("TVA : " + (Boolean.TRUE.equals(bl.getTvaApplicable()) ? bl.getMontantTVA() : "Désactivée"), body));
            Paragraph total = new Paragraph("Total TTC : " + bl.getMontantTTC(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)); total.setAlignment(Element.ALIGN_RIGHT); doc.add(total);
            doc.close(); return output.toByteArray();
        } catch (Exception e) { throw new IllegalStateException("Impossible de générer le PDF du bon de livraison.", e); }
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> bonsDeSortieDisponibles(Long ordreId) {
        OrdreReparation ordre = orRepository.findById(ordreId).orElseThrow(() -> new ResourceNotFoundException("Ordre de réparation introuvable."));
        return bsRepository.findByOrdreReparationIdOrderByDateDesc(ordreId).stream()
                .filter(bs -> bs.getStatut() == StatutBon.VALIDE)
                .map(bs -> Map.<String, Object>of("id", bs.getId(), "reference", bs.getReference(), "date", bs.getDate(),
                        "montant", Optional.ofNullable(bs.getMontantTotal()).orElse(BigDecimal.ZERO),
                        "clientId", bs.getClient().getId(), "vehiculeId", bs.getVehicule().getId(),
                        "ordreReparationId", ordre.getId()))
                .toList();
    }

    @Transactional
    public BonDeLivraisonResponse create(BonDeLivraisonCreateRequest req) {
        OrdreReparation ordre = orRepository.findByIdForFacturation(req.getOrdreReparationId())
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de réparation introuvable."));
        if (repository.existsByOrdreReparationId(ordre.getId())) throw new BadRequestException("Un bon de livraison existe déjà pour cet ordre de réparation.");
        if (factureRepository.findFirstByOrdreReparationId(ordre.getId()).isPresent()) throw new BadRequestException("Une facture existe déjà pour cet ordre. Le bon de livraison doit être créé avant la facture.");
        var bons = bsRepository.findAllById(new HashSet<>(req.getBonsDeSortieIds()));
        if (bons.size() != new HashSet<>(req.getBonsDeSortieIds()).size()) throw new BadRequestException("Un ou plusieurs bons de sortie sont introuvables.");
        if (bons.isEmpty()) throw new BadRequestException("Sélectionnez au moins un bon de sortie validé.");
        var client = ordre.getClient() != null ? ordre.getClient() : ordre.getVehicule() == null ? null : ordre.getVehicule().getClient();
        if (client == null || ordre.getVehicule() == null) throw new BadRequestException("L'ordre doit avoir un client et un véhicule.");
        for (BonDeSortie bs : bons) {
            if (bs.getStatut() != StatutBon.VALIDE) throw new BadRequestException("Seuls les bons de sortie validés peuvent être livrés.");
            if (bs.getOrdreReparation() == null || !ordre.getId().equals(bs.getOrdreReparation().getId())) throw new BadRequestException("Chaque bon de sortie doit être lié à l'ordre de réparation sélectionné.");
            if (bs.getClient() == null || !client.getId().equals(bs.getClient().getId()) || bs.getVehicule() == null || !ordre.getVehicule().getId().equals(bs.getVehicule().getId())) throw new BadRequestException("Le client ou le véhicule d'un bon de sortie ne correspond pas à l'ordre.");
        }
        Agent agent = currentAgent();
        boolean tva = req.getAppliquerTVA() == null || Boolean.TRUE.equals(req.getAppliquerTVA());
        BonDeLivraison bl = BonDeLivraison.builder().numero(numberGenerator.generateNextNumber(DocumentType.BL))
                .garage(ordre.getGarage()).client(client).vehicule(ordre.getVehicule()).ordreReparation(ordre)
                .agent(agent).kilometrage(0d)
                .remarque(req.getRemarque()).statut(StatutFacturation.EN_ATTENTE).bonsDeSortie(new ArrayList<>(bons))
                .lignesFacturationPieces(new ArrayList<>()).lignesFacturationMainDoeuvres(new ArrayList<>()).tvaApplicable(tva).build();
        BigDecimal ht = BigDecimal.ZERO;
        for (BonDeSortie bs : bons) {
            for (LigneBonDeSortiePiece l : bs.getLignesBonDeSortiePieces()) {
                if (l.getQuantite() == null || l.getQuantite() <= 0) continue;
                BigDecimal remise = nz(l.getTauxRemise());
                if (l.getType() == TypeLigneBonDeSortie.MAIN_DOEUVRE) {
                    var mainDoeuvre = l.getMainDoeuvreId() == null ? null : mainDoeuvreRepository.findById(l.getMainDoeuvreId()).orElse(null);
                    if (mainDoeuvre == null) throw new BadRequestException("Une ancienne ligne de main-d'œuvre du bon de sortie n'est plus disponible.");
                    var row = LigneFacturationMainDoeuvre.builder().facturation(bl).mainDoeuvre(mainDoeuvre)
                            .nbreHeure(l.getQuantite()).tarifHoraire(l.getPrix()).tauxRemise(remise).build();
                    bl.getLignesFacturationMainDoeuvres().add(row);
                    ht = ht.add(net(BigDecimal.valueOf((long) l.getPrix() * l.getQuantite()), remise));
                    continue;
                }
                var row = LigneFacturationPiece.builder().facturation(bl).piece(l.getPiece())
                        .isCustom(l.getPiece() == null).designationPds(l.getDesignation())
                        .quantite(l.getQuantite()).prix(l.getPrix()).tauxRemise(remise).build();
                bl.getLignesFacturationPieces().add(row);
                ht = ht.add(net(BigDecimal.valueOf((long) l.getPrix() * l.getQuantite()), remise));
            }
            for (LigneBonDeSortieMainDoeuvre l : bs.getLignesBonDeSortieMainDoeuvres()) {
                if (l.getMainDoeuvre() == null || l.getNbreHeure() == null || l.getNbreHeure() <= 0) continue;
                var row = LigneFacturationMainDoeuvre.builder().facturation(bl).mainDoeuvre(l.getMainDoeuvre())
                        .nbreHeure(l.getNbreHeure()).tarifHoraire(l.getTarifHoraire()).tauxRemise(nz(l.getTauxRemise())).build();
                bl.getLignesFacturationMainDoeuvres().add(row);
                ht = ht.add(net(BigDecimal.valueOf((long) l.getTarifHoraire() * l.getNbreHeure()), l.getTauxRemise()));
            }
        }
        var fiscal = fiscalService.getCurrent();
        BigDecimal montantTVA = tva ? fiscalService.calculateRate(ht, fiscal.getTvaRate()) : BigDecimal.ZERO;
        bl.setMontantHT(ht); bl.setMontantTVA(montantTVA); bl.setMontantTimbre(BigDecimal.ZERO);
        bl.setMontantTTC(ht.add(montantTVA)); bl.setMontantTotal(ht.add(montantTVA));
        return BonDeLivraisonResponse.from(repository.save(bl));
    }

    @Transactional
    public Facture convertirEnFacture(Long id) {
        BonDeLivraison bl = repository.findByIdForConversion(id).orElseThrow(() -> new ResourceNotFoundException("Bon de livraison introuvable."));
        if (bl.getFacture() != null) return bl.getFacture();
        if (factureRepository.findFirstByOrdreReparationId(bl.getOrdreReparation().getId()).isPresent()) throw new BadRequestException("Une facture existe déjà pour cet ordre de réparation.");
        Facture facture = Facture.builder().numero(numberGenerator.generateNextNumber(DocumentType.FC)).garage(bl.getGarage())
                .client(bl.getClient()).vehicule(bl.getVehicule()).ordreReparation(bl.getOrdreReparation()).bonDeLivraison(bl)
                .agent(currentAgent()).kilometrage(bl.getKilometrage()).remarque(bl.getRemarque()).statut(StatutFacturation.EN_ATTENTE)
                .tvaApplicable(bl.getTvaApplicable()).montantHT(bl.getMontantHT()).montantTVA(bl.getMontantTVA())
                .montantTTC(bl.getMontantTTC()).montantTotal(bl.getMontantTTC()).montantTimbre(BigDecimal.ZERO)
                .montantPaye(BigDecimal.ZERO).resteAPayer(bl.getMontantTTC()).statutPaiement(StatutPaiement.NON_PAYE)
                .lignesFacturationPieces(new ArrayList<>()).lignesFacturationMainDoeuvres(new ArrayList<>()).build();
        if (bl.getClient().getEcheance() != null && bl.getClient().getEcheance() > 0)
            facture.setDateEcheance(java.time.LocalDateTime.now().plusDays(bl.getClient().getEcheance()));
        for (var row : copyPieces(bl, facture)) facture.getLignesFacturationPieces().add(row);
        for (var row : copyMo(bl, facture)) facture.getLignesFacturationMainDoeuvres().add(row);
        for (var l : facture.getLignesFacturationPieces()) {
            if (l.getPiece() instanceof PDP p) {
                double before = p.getQteReelle() == null ? p.getStockMagasin() + p.getStockAtelier() : p.getQteReelle();
                p.setQteReelle(Math.max(0, before - l.getQuantite())); pieceRepository.save(p);
                mouvementRepository.save(PieceMouvement.builder().type(TypeMouvement.SORTIE_REELLE).quantite((double) l.getQuantite())
                        .stockMagasinAvant(p.getStockMagasin()).stockAtelierAvant(p.getStockAtelier()).stockMagasinApres(p.getStockMagasin())
                        .stockAtelierApres(p.getStockAtelier()).stockReelApres(p.getQteReelle()).numDocument(facture.getNumero())
                        .typeDocument("Facture").numeroSerie(p.getReference()).immatriculation(bl.getVehicule().getImmatriculation())
                        .motif("Facture " + facture.getNumero()).piece(p).agent(facture.getAgent()).garage(bl.getGarage())
                        .prenom(bl.getClient().getFirstName()).nom(bl.getClient().getLastName()).build());
            }
        }
        if (bl.getOrdreReparation().getStatut() == StatutOrdreReparation.ORDRE_DE_TRAVAIL || bl.getOrdreReparation().getStatut() == StatutOrdreReparation.PROFORMA) bl.getOrdreReparation().setStatut(StatutOrdreReparation.PAIEMENT);
        if (bl.getOrdreReparation().getProforma() != null)
            bl.getOrdreReparation().getProforma().setStatut(StatutFacturation.CONVERTIE);
        return factureRepository.save(facture);
    }

    @Transactional
    public NoteDePrix convertirEnNoteDePrix(Long id) {
        BonDeLivraison bl = repository.findByIdForConversion(id).orElseThrow(() -> new ResourceNotFoundException("Bon de livraison introuvable."));
        if (bl.getNoteDePrix() != null) return bl.getNoteDePrix();
        NoteDePrix note = NoteDePrix.builder().numero(numberGenerator.generateNextNumber(DocumentType.NP)).garage(bl.getGarage())
                .client(bl.getClient()).vehicule(bl.getVehicule()).ordreReparation(bl.getOrdreReparation()).bonDeLivraison(bl)
                .agent(currentAgent()).kilometrage(bl.getKilometrage()).remarque(bl.getRemarque()).statut(StatutFacturation.EN_ATTENTE)
                .tvaApplicable(bl.getTvaApplicable()).montantHT(bl.getMontantHT()).montantTVA(bl.getMontantTVA())
                .montantTotal(bl.getMontantTTC()).montantPaye(BigDecimal.ZERO).resteAPayer(bl.getMontantTTC())
                .statutPaiement(StatutPaiement.NON_PAYE).modePaiement("ESPECE")
                .lignesFacturationPieces(new ArrayList<>()).lignesFacturationMainDoeuvres(new ArrayList<>()).build();
        for (var row : copyPieces(bl, note)) note.getLignesFacturationPieces().add(row);
        for (var row : copyMo(bl, note)) note.getLignesFacturationMainDoeuvres().add(row);
        return noteRepository.save(note);
    }

    private List<LigneFacturationPiece> copyPieces(BonDeLivraison source, sn.oas.facturation.features.facturation.data.entity.Facturation target) {
        return source.getLignesFacturationPieces().stream().map(l -> LigneFacturationPiece.builder().piece(l.getPiece())
                .facturation(target).isCustom(l.getIsCustom()).designationPds(l.getDesignationPds()).quantite(l.getQuantite())
                .prix(l.getPrix()).tauxRemise(nz(l.getTauxRemise())).build()).toList();
    }
    private List<LigneFacturationMainDoeuvre> copyMo(BonDeLivraison source, sn.oas.facturation.features.facturation.data.entity.Facturation target) {
        return source.getLignesFacturationMainDoeuvres().stream().map(l -> LigneFacturationMainDoeuvre.builder().mainDoeuvre(l.getMainDoeuvre())
                .facturation(target).nbreHeure(l.getNbreHeure()).tarifHoraire(l.getTarifHoraire()).tauxRemise(nz(l.getTauxRemise())).build()).toList();
    }
    private BonDeLivraison find(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bon de livraison introuvable.")); }
    private Agent currentAgent() {
        var auth = SecurityContextHolder.getContext().getAuthentication(); if (auth == null) return null;
        User user = userRepository.findByUsername(auth.getName()).or(() -> userRepository.findByEmail(auth.getName())).orElse(null);
        return user instanceof Agent a ? a : null;
    }
    private BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private BigDecimal net(BigDecimal base, BigDecimal rate) { return rate.signum() <= 0 ? base : base.subtract(base.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)); }
}
