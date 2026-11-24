package sn.oas.facturation.features.tva.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.avoirTTC.data.entity.AvoirTTC;
import sn.oas.facturation.features.avoirTTC.repository.AvoirTTCRepository;
import sn.oas.facturation.features.avoirTvaAchetee.data.entity.AvoirTvaAchetee;
import sn.oas.facturation.features.avoirTvaAchetee.dto.AvoirTvaAcheteeResponse;
import sn.oas.facturation.features.avoirTvaAchetee.repository.AvoirTvaAcheteeRepository;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.factureAchetee.data.entity.FactureAchetee;
import sn.oas.facturation.features.factureAchetee.dto.FactureAcheteeResponse;
import sn.oas.facturation.features.factureAchetee.repository.FactureAcheteeRepository;
import sn.oas.facturation.features.pdfGenerator.service.HtmlToPdfService;
import sn.oas.facturation.features.tva.data.entity.RapportTvaMensuel;
import sn.oas.facturation.features.tva.dto.TvaDeclarationResponseDto;
import sn.oas.facturation.features.tva.dto.TvaRapportUpdateRequest;
import sn.oas.facturation.features.tva.dto.TvaVenteItemDto;
import sn.oas.facturation.features.tva.repository.RapportTvaMensuelRepository;
import sn.oas.facturation.shared.dto.PageResponse;
import sn.oas.facturation.shared.exception.BadRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TvaDeclarationServiceImpl implements TvaDeclarationService {

    private static final int MAX_PAGE_SIZE = 500;
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FactureRepository factureRepository;
    private final AvoirTTCRepository avoirTTCRepository;
    private final FactureAcheteeRepository factureAcheteeRepository;
    private final AvoirTvaAcheteeRepository avoirTvaAcheteeRepository;
    private final RapportTvaMensuelRepository rapportRepository;
    private final HtmlToPdfService htmlToPdfService;

    @Override
    @Transactional
    public TvaDeclarationResponseDto getDeclaration(
            YearMonth month,
            int pageVendu,
            int pageAvoirVendu,
            int pageAchete,
            int pageAvoirAchete,
            int size) {

        YearMonth target = month != null ? month : YearMonth.now();
        YearMonth previous = target.minusMonths(1);
        int effectiveSize = size > 0 ? Math.min(size, MAX_PAGE_SIZE) : 10;

        LocalDate dateDebut = target.atDay(1);
        LocalDate dateFin = target.atEndOfMonth();
        LocalDateTime dtDebut = dateDebut.atStartOfDay();
        LocalDateTime dtFinExclusive = dateFin.plusDays(1).atStartOfDay();

        Page<TvaVenteItemDto> facturesPage = factureRepository
                .findByDateCreationGreaterThanEqualAndDateCreationLessThanOrderByDateCreationDescIdDesc(
                        dtDebut, dtFinExclusive, PageRequest.of(Math.max(0, pageVendu), effectiveSize))
                .map(TvaVenteItemDto::fromFacture);

        Page<TvaVenteItemDto> avoirsPage = avoirTTCRepository
                .findByDateCreationGreaterThanEqualAndDateCreationLessThanOrderByDateCreationDescIdDesc(
                        dtDebut, dtFinExclusive, PageRequest.of(Math.max(0, pageAvoirVendu), effectiveSize))
                .map(TvaVenteItemDto::fromAvoirTTC);

        Page<FactureAcheteeResponse> facturesAcheteesPage = factureAcheteeRepository
                .findByDateFactureBetweenOrderByDateFactureDescIdDesc(
                        dateDebut, dateFin, PageRequest.of(Math.max(0, pageAchete), effectiveSize))
                .map(FactureAcheteeResponse::from);

        Page<AvoirTvaAcheteeResponse> avoirsAchetesPage = avoirTvaAcheteeRepository
                .findByDateOperationBetweenOrderByDateOperationDescIdDesc(
                        dateDebut, dateFin, PageRequest.of(Math.max(0, pageAvoirAchete), effectiveSize))
                .map(AvoirTvaAcheteeResponse::from);

        MonthlyTotals totals = computeAndSyncMonthlyTotals(target, previous, dateDebut, dateFin, dtDebut, dtFinExclusive);

        return TvaDeclarationResponseDto.builder()
                .month(target.toString())
                .previousMonth(previous.toString())
                .facturesVendues(PageResponse.from(facturesPage))
                .avoirsVendus(PageResponse.from(avoirsPage))
                .facturesAchetees(PageResponse.from(facturesAcheteesPage))
                .avoirsTvaAchetees(PageResponse.from(avoirsAchetesPage))
                .totalTvaVendue(totals.totalTvaVendue())
                .totalTimbreVendu(totals.totalTimbreVendu())
                .totalTvaAvoirVendu(totals.totalTvaAvoirVendu())
                .totalTimbreAvoirVendu(totals.totalTimbreAvoirVendu())
                .rapportTvaVendue(totals.rapportTvaVendue())
                .totalTimbre(totals.totalTimbre())
                .totalTvaAchetee(totals.totalTvaAchetee())
                .totalAvoirTvaAchetee(totals.totalAvoirTvaAchetee())
                .rapportTvaAchetee(totals.rapportTvaAchetee())
                .rapport(totals.rapport())
                .difference(totals.difference())
                .solde(totals.solde())
                .build();
    }

    @Override
    @Transactional
    public byte[] generateDeclarationPdf(YearMonth month) {
        YearMonth target = month != null ? month : YearMonth.now();
        YearMonth previous = target.minusMonths(1);
        LocalDate dateDebut = target.atDay(1);
        LocalDate dateFin = target.atEndOfMonth();
        LocalDateTime dtDebut = dateDebut.atStartOfDay();
        LocalDateTime dtFinExclusive = dateFin.plusDays(1).atStartOfDay();

        List<Facture> facturesVendues = factureRepository
                .findByDateCreationGreaterThanEqualAndDateCreationLessThanOrderByDateCreationDescIdDesc(dtDebut, dtFinExclusive);
        List<AvoirTTC> avoirsVendus = avoirTTCRepository
                .findByDateCreationGreaterThanEqualAndDateCreationLessThanOrderByDateCreationDescIdDesc(dtDebut, dtFinExclusive);
        List<FactureAchetee> facturesAchetees = factureAcheteeRepository
                .findByDateFactureBetweenOrderByDateFactureDescIdDesc(dateDebut, dateFin);
        List<AvoirTvaAchetee> avoirsAchetes = avoirTvaAcheteeRepository
                .findByDateOperationBetweenOrderByDateOperationDescIdDesc(dateDebut, dateFin);

        MonthlyTotals totals = computeAndSyncMonthlyTotals(target, previous, dateDebut, dateFin, dtDebut, dtFinExclusive);
        String xhtml = buildPdfXhtml(target.toString(), facturesVendues, avoirsVendus, facturesAchetees, avoirsAchetes, totals);
        return htmlToPdfService.genererHtmlEnPdf(xhtml);
    }

    @Override
    @Transactional
    public RapportTvaMensuel updateRapport(TvaRapportUpdateRequest request) {
        if (request == null || request.month() == null || request.month().isBlank()) {
            throw new BadRequestException("Le mois (YYYY-MM) est obligatoire.");
        }
        YearMonth ym;
        try {
            ym = YearMonth.parse(request.month().trim());
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Format de mois invalide (attendu YYYY-MM) : " + request.month());
        }
        if (request.montant() == null) {
            throw new BadRequestException("Le montant du report est obligatoire.");
        }
        if (request.montant().compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("Un report de crédit de TVA doit être négatif ou nul (<= 0).");
        }

        BigDecimal normalized = request.montant().setScale(2, RoundingMode.HALF_UP);
        boolean lock = request.verrouilleManuellement() == null || request.verrouilleManuellement();
        String periode = ym.toString();

        RapportTvaMensuel entity = rapportRepository.findFirstByPeriodeOrderByIdDesc(periode)
                .orElseGet(() -> RapportTvaMensuel.builder().periode(periode).build());
        entity.setMontant(normalized);
        entity.setVerrouilleManuellement(lock);
        return rapportRepository.save(entity);
    }

    private MonthlyTotals computeAndSyncMonthlyTotals(
            YearMonth target,
            YearMonth previous,
            LocalDate dateDebut,
            LocalDate dateFin,
            LocalDateTime dtDebut,
            LocalDateTime dtFinExclusive) {

        BigDecimal totalTvaVendue = nz(factureRepository.sumMontantTvaBetween(dtDebut, dtFinExclusive));
        BigDecimal totalTimbreVendu = nz(factureRepository.sumMontantTimbreBetween(dtDebut, dtFinExclusive));
        BigDecimal totalTvaAvoirVendu = nz(avoirTTCRepository.sumMontantTvaBetween(dtDebut, dtFinExclusive));
        BigDecimal totalTimbreAvoirVendu = nz(avoirTTCRepository.sumMontantTimbreBetween(dtDebut, dtFinExclusive));

        BigDecimal totalTvaAchetee = nz(factureAcheteeRepository.sumMontantTvaBetween(dateDebut, dateFin));
        BigDecimal totalAvoirTvaAchetee = nz(avoirTvaAcheteeRepository.sumMontantTvaBetween(dateDebut, dateFin));

        BigDecimal rapportTvaVendue = totalTvaVendue.subtract(totalTvaAvoirVendu);
        BigDecimal totalTimbre = totalTimbreVendu.subtract(totalTimbreAvoirVendu);
        BigDecimal rapportTvaAchetee = totalTvaAchetee.subtract(totalAvoirTvaAchetee);

        BigDecimal rapportMoisPrecedent = rapportRepository
                .findFirstByPeriodeOrderByIdDesc(previous.toString())
                .map(RapportTvaMensuel::getMontant)
                .filter(m -> m != null && m.compareTo(BigDecimal.ZERO) < 0)
                .orElse(BigDecimal.ZERO);

        BigDecimal difference = rapportTvaVendue.subtract(rapportTvaAchetee);
        BigDecimal solde = difference.add(rapportMoisPrecedent);

        syncCurrentMonthRapport(target.toString(), solde);

        return new MonthlyTotals(
                totalTvaVendue,
                totalTimbreVendu,
                totalTvaAvoirVendu,
                totalTimbreAvoirVendu,
                rapportTvaVendue,
                totalTimbre,
                totalTvaAchetee,
                totalAvoirTvaAchetee,
                rapportTvaAchetee,
                rapportMoisPrecedent,
                difference,
                solde);
    }

    private void syncCurrentMonthRapport(String periode, BigDecimal solde) {
        BigDecimal reportCible = solde.compareTo(BigDecimal.ZERO) < 0
                ? solde.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        RapportTvaMensuel existing = rapportRepository.findFirstByPeriodeOrderByIdDesc(periode).orElse(null);
        if (existing != null) {
            if (existing.isVerrouilleManuellement()) {
                return;
            }
            if (existing.getMontant() == null || existing.getMontant().compareTo(reportCible) != 0) {
                existing.setMontant(reportCible);
                rapportRepository.save(existing);
            }
        } else if (reportCible.compareTo(BigDecimal.ZERO) < 0) {
            rapportRepository.save(RapportTvaMensuel.builder()
                    .periode(periode)
                    .montant(reportCible)
                    .verrouilleManuellement(false)
                    .build());
        }
    }

    private String buildPdfXhtml(
            String monthLabel,
            List<Facture> facturesVendues,
            List<AvoirTTC> avoirsVendus,
            List<FactureAchetee> facturesAchetees,
            List<AvoirTvaAchetee> avoirsAchetes,
            MonthlyTotals t) {

        StringBuilder sb = new StringBuilder(4096);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
        sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"fr\" lang=\"fr\">\n<head>\n");
        sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
        sb.append("<title>Liste des factures acheté/vendu - ").append(esc(monthLabel)).append("</title>\n");
        sb.append("<style type=\"text/css\">\n");
        sb.append("@page { size: A4 portrait; margin: 12mm 10mm; }\n");
        sb.append("body { font-family: Arial, Helvetica, sans-serif; font-size: 11px; color: #111; }\n");
        sb.append(".section-title { text-align: center; margin: 14px 0 8px 0; }\n");
        sb.append(".section-title span { background-color: #63AFDE; font-size: 14px; font-weight: bold; padding: 3px 10px; }\n");
        sb.append("table.data { width: 100%; border-collapse: collapse; margin-bottom: 10px; }\n");
        sb.append("table.data th { background-color: #63AFDE; border: 1px solid #333; padding: 4px 6px; text-align: center; font-weight: bold; }\n");
        sb.append("table.data td { border: 1px solid #555; padding: 4px 6px; text-align: center; }\n");
        sb.append(".synthese-title { text-align: center; font-size: 14px; font-weight: bold; margin: 22px 0 14px 0; }\n");
        sb.append("table.kpi { width: 100%; border-collapse: collapse; margin: 8px 0; }\n");
        sb.append("table.kpi td { width: 50%; text-align: center; padding: 8px 4px; vertical-align: top; }\n");
        sb.append(".kpi-label { font-size: 13px; font-weight: bold; color: #000; margin-bottom: 3px; }\n");
        sb.append(".kpi-val { font-size: 13px; color: #2e8b57; }\n");
        sb.append("hr.sep { border: 0; border-top: 1.5px solid #000; margin: 14px 0; }\n");
        sb.append(".center-kpi { text-align: center; margin: 10px 0; }\n");
        sb.append("</style>\n</head>\n<body>\n");

        // 1. Factures vendues
        sb.append("<div class=\"section-title\"><span>Liste des factures vendues du mois de ").append(esc(monthLabel)).append("</span></div>\n");
        sb.append("<table class=\"data\"><thead><tr><th>Num. facture</th><th>Date</th><th>Tva</th><th>Timbre</th></tr></thead><tbody>\n");
        for (Facture f : facturesVendues) {
            sb.append("<tr><td>").append(esc(f.getNumero())).append("</td><td>")
                    .append(f.getDateCreation() != null ? esc(f.getDateCreation().format(DT_FORMAT)) : "")
                    .append("</td><td>").append(fmtPlain(f.getMontantTVA())).append("</td><td>")
                    .append(fmtPlain(f.getMontantTimbre())).append("</td></tr>\n");
        }
        sb.append("</tbody></table>\n");

        // 2. Avoirs vendus
        sb.append("<div class=\"section-title\"><span>Liste des avoirs du mois de ").append(esc(monthLabel)).append("</span></div>\n");
        sb.append("<table class=\"data\"><thead><tr><th>Num. Avoir</th><th>Date</th><th>TVA</th><th>Timbre</th></tr></thead><tbody>\n");
        for (AvoirTTC a : avoirsVendus) {
            sb.append("<tr><td>").append(esc(a.getNumero())).append("</td><td>")
                    .append(a.getDateCreation() != null ? esc(a.getDateCreation().format(DT_FORMAT)) : "")
                    .append("</td><td>").append(fmtPlain(a.getMontantTVA())).append("</td><td>")
                    .append(fmtPlain(a.getMontantTimbre())).append("</td></tr>\n");
        }
        sb.append("</tbody></table>\n");

        // 3. Factures achetées
        sb.append("<div class=\"section-title\"><span>Liste des factures achetées du mois de ").append(esc(monthLabel)).append("</span></div>\n");
        sb.append("<table class=\"data\"><thead><tr><th>Num. facture</th><th>Date</th><th>Tva</th></tr></thead><tbody>\n");
        for (FactureAchetee fa : facturesAchetees) {
            sb.append("<tr><td>").append(esc(fa.getNumeroFacture())).append("</td><td>")
                    .append(fa.getDateFacture() != null ? esc(fa.getDateFacture().toString()) : "")
                    .append("</td><td>").append(fmtPlain(fa.getMontantTva())).append("</td></tr>\n");
        }
        sb.append("</tbody></table>\n");

        // 4. Avoir TVA achetées
        sb.append("<div class=\"section-title\"><span>Liste des Avoir TVA achetées du mois de ").append(esc(monthLabel)).append("</span></div>\n");
        sb.append("<table class=\"data\"><thead><tr><th>Designation</th><th>Date</th><th>Tva</th></tr></thead><tbody>\n");
        for (AvoirTvaAchetee aa : avoirsAchetes) {
            sb.append("<tr><td>").append(esc(aa.getDesignation())).append("</td><td>")
                    .append(aa.getDateOperation() != null ? esc(aa.getDateOperation().toString()) : "")
                    .append("</td><td>").append(fmtPlain(aa.getMontantTva())).append("</td></tr>\n");
        }
        sb.append("</tbody></table>\n");

        // 5. Synthèse générale
        sb.append("<div class=\"synthese-title\">SYNTHESE GENERALE</div>\n");
        sb.append("<table class=\"kpi\"><tr>");
        sb.append("<td><div class=\"kpi-label\">Total TVA vendue :</div><div class=\"kpi-val\">").append(fmtFcfa(t.totalTvaVendue())).append("</div></td>");
        sb.append("<td><div class=\"kpi-label\">Total TVA achetée :</div><div class=\"kpi-val\">").append(fmtFcfa(t.totalTvaAchetee())).append("</div></td>");
        sb.append("</tr><tr>");
        sb.append("<td><div class=\"kpi-label\">Avoir TVA vendue :</div><div class=\"kpi-val\">").append(fmtFcfa(t.totalTvaAvoirVendu())).append("</div></td>");
        sb.append("<td><div class=\"kpi-label\">Avoir TVA achetée :</div><div class=\"kpi-val\">").append(fmtFcfa(t.totalAvoirTvaAchetee())).append("</div></td>");
        sb.append("</tr></table>\n");

        sb.append("<hr class=\"sep\" />\n");

        sb.append("<table class=\"kpi\"><tr>");
        sb.append("<td><div class=\"kpi-label\">Rapport TVA vendue :</div><div class=\"kpi-val\">").append(fmtFcfa(t.rapportTvaVendue())).append("</div></td>");
        sb.append("<td><div class=\"kpi-label\">Rapport TVA achetée :</div><div class=\"kpi-val\">").append(fmtFcfa(t.rapportTvaAchetee())).append("</div></td>");
        sb.append("</tr></table>\n");

        if (t.rapport().compareTo(BigDecimal.ZERO) < 0) {
            sb.append("<div class=\"center-kpi\"><div class=\"kpi-label\">Report mois précédent :</div><div class=\"kpi-val\">")
                    .append(fmtFcfa(t.rapport())).append("</div></div>\n");
        }

        sb.append("<div class=\"center-kpi\"><div class=\"kpi-label\">Solde TVA</div><div class=\"kpi-val\">")
                .append(fmtFcfa(t.solde())).append("</div></div>\n");

        sb.append("<hr class=\"sep\" />\n");

        sb.append("<div class=\"center-kpi\"><div class=\"kpi-label\">Total timbre :</div><div class=\"kpi-val\">")
                .append(fmtFcfa(t.totalTimbre())).append("</div></div>\n");

        sb.append("</body></html>");
        return sb.toString();
    }

    private String fmtFcfa(BigDecimal value) {
        BigDecimal v = nz(value).setScale(0, RoundingMode.HALF_UP);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,##0", symbols);
        return df.format(v) + " F CFA";
    }

    private String fmtPlain(BigDecimal value) {
        return nz(value).setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private record MonthlyTotals(
            BigDecimal totalTvaVendue,
            BigDecimal totalTimbreVendu,
            BigDecimal totalTvaAvoirVendu,
            BigDecimal totalTimbreAvoirVendu,
            BigDecimal rapportTvaVendue,
            BigDecimal totalTimbre,
            BigDecimal totalTvaAchetee,
            BigDecimal totalAvoirTvaAchetee,
            BigDecimal rapportTvaAchetee,
            BigDecimal rapport,
            BigDecimal difference,
            BigDecimal solde) {
    }
}
