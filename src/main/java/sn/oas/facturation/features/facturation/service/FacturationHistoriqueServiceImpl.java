package sn.oas.facturation.features.facturation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.avoirHT.data.entity.AvoirHT;
import sn.oas.facturation.features.avoirHT.repository.AvoirHTRepository;
import sn.oas.facturation.features.avoirTTC.data.entity.AvoirTTC;
import sn.oas.facturation.features.avoirTTC.repository.AvoirTTCRepository;
import sn.oas.facturation.features.bonDeLivraison.data.entity.BonDeLivraison;
import sn.oas.facturation.features.bonDeLivraison.repository.BonDeLivraisonRepository;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.facturation.data.entity.Facturation;
import sn.oas.facturation.features.facturation.data.entity.FactureTTC;
import sn.oas.facturation.features.facturation.data.enums.TypeHistoriqueFacturation;
import sn.oas.facturation.features.facturation.dto.FacturationHistoriqueItemDto;
import sn.oas.facturation.features.facturation.dto.FacturationHistoriqueResponseDto;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.features.noteDePrix.repository.NoteDePrixRepository;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreTravail.data.entity.OrdreTravail;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.shared.exception.BadRequestException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FacturationHistoriqueServiceImpl implements FacturationHistoriqueService {

    private static final int MAX_PAGE_SIZE = 500;
    private static final LocalDateTime MIN_DATE = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime MAX_DATE = LocalDateTime.of(3000, 1, 1, 0, 0);

    private final BonDeLivraisonRepository bonDeLivraisonRepository;
    private final FactureRepository factureRepository;
    private final AvoirTTCRepository avoirTTCRepository;
    private final NoteDePrixRepository noteDePrixRepository;
    private final AvoirHTRepository avoirHTRepository;

    @Override
    @Transactional(readOnly = true)
    public FacturationHistoriqueResponseDto getHistorique(
            TypeHistoriqueFacturation type,
            LocalDate date,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long clientId,
            String clientNumero,
            String keyword,
            Integer page,
            Integer size) {

        LocalDate effDebut;
        LocalDate effFin;
        if (dateDebut != null && dateFin != null) {
            effDebut = dateDebut;
            effFin = dateFin;
        } else if (date != null) {
            effDebut = date;
            effFin = date;
        } else if (dateDebut != null) {
            effDebut = dateDebut;
            effFin = dateDebut;
        } else if (dateFin != null) {
            effDebut = dateFin;
            effFin = dateFin;
        } else {
            effDebut = null;
            effFin = null;
        }

        if (effDebut != null && effFin != null && effDebut.isAfter(effFin)) {
            throw new BadRequestException("La date de début doit être antérieure ou égale à la date de fin.");
        }

        LocalDateTime borneDebut = effDebut != null ? effDebut.atStartOfDay() : MIN_DATE;
        LocalDateTime borneFinExclusive = effFin != null ? effFin.plusDays(1).atStartOfDay() : MAX_DATE;

        List<FacturationHistoriqueItemDto> allEvents = new ArrayList<>();
        bonDeLivraisonRepository.findHistoriqueBetween(borneDebut, borneFinExclusive)
                .forEach(bl -> allEvents.add(mapBonLivraison(bl)));
        factureRepository.findHistoriqueBetween(borneDebut, borneFinExclusive)
                .forEach(f -> allEvents.add(mapFacture(f)));
        avoirTTCRepository.findHistoriqueBetween(borneDebut, borneFinExclusive)
                .forEach(a -> allEvents.add(mapAvoirTTC(a)));
        noteDePrixRepository.findHistoriqueBetween(borneDebut, borneFinExclusive)
                .forEach(n -> allEvents.add(mapNoteDePrix(n)));
        avoirHTRepository.findHistoriqueBetween(borneDebut, borneFinExclusive)
                .forEach(a -> allEvents.add(mapAvoirHT(a)));

        allEvents.sort(Comparator
                .comparing(FacturationHistoriqueItemDto::getDateCreation, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(FacturationHistoriqueItemDto::getId, Comparator.nullsLast(Comparator.reverseOrder())));

        String normalizedClientNum = normalize(clientNumero);
        String normalizedKw = normalize(keyword);

        List<FacturationHistoriqueItemDto> scopedEvents = allEvents.stream()
                .filter(item -> matchesClient(item, clientId, normalizedClientNum))
                .filter(item -> normalizedKw == null || matchesKeyword(item, normalizedKw))
                .toList();

        long totalBl = 0;
        long totalFactures = 0;
        long totalAvoirsTtc = 0;
        long totalNotesPrix = 0;
        long totalAvoirsHt = 0;
        BigDecimal totalMontantBlTtc = BigDecimal.ZERO;
        BigDecimal totalMontantFacturesTtc = BigDecimal.ZERO;
        BigDecimal totalMontantAvoirsTtc = BigDecimal.ZERO;
        BigDecimal totalMontantNotesPrixHt = BigDecimal.ZERO;
        BigDecimal totalMontantAvoirsHt = BigDecimal.ZERO;

        for (FacturationHistoriqueItemDto item : scopedEvents) {
            switch (item.getType()) {
                case BL -> {
                    totalBl++;
                    totalMontantBlTtc = totalMontantBlTtc.add(nz(item.getMontantTotal()));
                }
                case FACTURE -> {
                    totalFactures++;
                    totalMontantFacturesTtc = totalMontantFacturesTtc.add(nz(item.getMontantTotal()));
                }
                case AVOIR_TTC -> {
                    totalAvoirsTtc++;
                    totalMontantAvoirsTtc = totalMontantAvoirsTtc.add(nz(item.getMontantTotal()));
                }
                case NOTE_PRIX -> {
                    totalNotesPrix++;
                    totalMontantNotesPrixHt = totalMontantNotesPrixHt.add(nz(item.getMontantTotal()));
                }
                case AVOIR_HT -> {
                    totalAvoirsHt++;
                    totalMontantAvoirsHt = totalMontantAvoirsHt.add(nz(item.getMontantTotal()));
                }
            }
        }

        List<FacturationHistoriqueItemDto> filtered = type == null
                ? scopedEvents
                : scopedEvents.stream().filter(item -> item.getType() == type).toList();

        long totalElements = filtered.size();
        Integer effectivePage = null;
        Integer effectiveSize = null;
        int totalPages;
        List<FacturationHistoriqueItemDto> resultEvents;

        if (page != null || size != null) {
            effectivePage = Math.max(0, page != null ? page : 0);
            effectiveSize = size != null ? Math.min(Math.max(1, size), MAX_PAGE_SIZE) : 10;
            totalPages = (int) Math.ceil((double) totalElements / effectiveSize);
            int fromIndex = effectivePage * effectiveSize;
            if (fromIndex >= filtered.size()) {
                resultEvents = List.of();
            } else {
                int toIndex = Math.min(fromIndex + effectiveSize, filtered.size());
                resultEvents = filtered.subList(fromIndex, toIndex);
            }
        } else {
            totalPages = totalElements > 0 ? 1 : 0;
            resultEvents = filtered;
        }

        return FacturationHistoriqueResponseDto.builder()
                .dateDebut(effDebut)
                .dateFin(effFin)
                .clientId(clientId)
                .clientNumero(clientNumero != null && !clientNumero.isBlank() ? clientNumero.trim() : null)
                .totalOperations(scopedEvents.size())
                .totalBonsLivraison(totalBl)
                .totalFactures(totalFactures)
                .totalAvoirsTTC(totalAvoirsTtc)
                .totalNotesDePrix(totalNotesPrix)
                .totalAvoirsHT(totalAvoirsHt)
                .montantTotalBonsLivraisonTTC(totalMontantBlTtc)
                .montantTotalFacturesTTC(totalMontantFacturesTtc)
                .montantTotalAvoirsTTC(totalMontantAvoirsTtc)
                .montantTotalNotesDePrixHT(totalMontantNotesPrixHt)
                .montantTotalAvoirsHT(totalMontantAvoirsHt)
                .page(effectivePage)
                .size(effectiveSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .evenements(resultEvents)
                .build();
    }

    private FacturationHistoriqueItemDto mapBonLivraison(BonDeLivraison bl) {
        String statut = bl.getFacture() != null
                ? "FACTURÉ"
                : (bl.getNoteDePrix() != null ? "NOTE DE PRIX" : "CRÉÉ");
        return buildItem(
                bl,
                bl.getOrdreTravail(),
                TypeHistoriqueFacturation.BL,
                "Bon de livraison",
                firstNonBlank(bl.getNumero(), "BL #" + bl.getId()),
                bl.getMontantTTC() != null ? bl.getMontantTTC() : bl.getMontantTotal(),
                null,
                null,
                statut);
    }

    private FacturationHistoriqueItemDto mapFacture(Facture f) {
        BigDecimal total = f.getMontantTotal() != null ? f.getMontantTotal() : f.getMontantTTC();
        BigDecimal paye = nz(f.getMontantPaye());
        BigDecimal reste = f.getResteAPayer() != null
                ? f.getResteAPayer()
                : nz(total).subtract(paye).max(BigDecimal.ZERO);
        String statut = f.getStatutPaiement() != null ? f.getStatutPaiement().name() : "NON_PAYE";

        return buildItem(
                f,
                null,
                TypeHistoriqueFacturation.FACTURE,
                "Facture TTC",
                firstNonBlank(f.getNumero(), "FC #" + f.getId()),
                total,
                paye,
                reste,
                statut);
    }

    private FacturationHistoriqueItemDto mapAvoirTTC(AvoirTTC a) {
        BigDecimal total = a.getMontantTTC() != null ? a.getMontantTTC() : a.getMontantTotal();
        String statut = a.getStatut() != null ? a.getStatut().name() : "EN_ATTENTE";
        return buildItem(
                a,
                null,
                TypeHistoriqueFacturation.AVOIR_TTC,
                "Avoir TTC",
                firstNonBlank(a.getNumero(), "AVT #" + a.getId()),
                total,
                null,
                null,
                statut);
    }

    private FacturationHistoriqueItemDto mapNoteDePrix(NoteDePrix n) {
        BigDecimal total = n.getMontantTotal() != null ? n.getMontantTotal() : n.getMontantHT();
        BigDecimal paye = nz(n.getMontantPaye());
        BigDecimal reste = n.getResteAPayer() != null
                ? n.getResteAPayer()
                : nz(total).subtract(paye).max(BigDecimal.ZERO);
        String statut = n.getStatutPaiement() != null
                ? n.getStatutPaiement().name()
                : (n.getStatut() != null ? n.getStatut().name() : "NON_PAYE");

        return buildItem(
                n,
                null,
                TypeHistoriqueFacturation.NOTE_PRIX,
                "Note de prix (HT)",
                firstNonBlank(n.getNumero(), "NP #" + n.getId()),
                total,
                paye,
                reste,
                statut);
    }

    private FacturationHistoriqueItemDto mapAvoirHT(AvoirHT a) {
        BigDecimal total = a.getMontantTotal() != null ? a.getMontantTotal() : a.getMontantHT();
        String statut = a.getStatut() != null ? a.getStatut().name() : "EN_ATTENTE";
        return buildItem(
                a,
                null,
                TypeHistoriqueFacturation.AVOIR_HT,
                "Avoir Note de prix (HT)",
                firstNonBlank(a.getNumero(), "AVH #" + a.getId()),
                total,
                null,
                null,
                statut);
    }

    private FacturationHistoriqueItemDto buildItem(
            Facturation doc,
            OrdreTravail ordreTravail,
            TypeHistoriqueFacturation type,
            String typeLabel,
            String numero,
            BigDecimal montantTotal,
            BigDecimal montantPaye,
            BigDecimal resteAPayer,
            String statut) {

        OrdreReparation ordre = doc.getOrdreReparation();
        Vehicule vehicule = resolveVehicule(doc, ordre, ordreTravail);
        Client client = resolveClient(doc, ordre, ordreTravail, vehicule);

        BigDecimal montantHT = null;
        BigDecimal montantTVA = null;
        BigDecimal montantTimbre = null;
        BigDecimal montantTTC = null;
        if (doc instanceof FactureTTC ttc) {
            montantHT = ttc.getMontantHT();
            montantTVA = ttc.getMontantTVA();
            montantTimbre = ttc.getMontantTimbre();
            montantTTC = ttc.getMontantTTC();
        } else if (doc instanceof sn.oas.facturation.features.facturation.data.entity.FactureHT ht) {
            montantHT = ht.getMontantHT();
        }

        String clientNumero = null;
        if (client != null) {
            clientNumero = firstNonBlank(
                    client.getCodeClient(),
                    client.getMatricule(),
                    client.getId() != null ? String.valueOf(client.getId()) : null);
        }

        return FacturationHistoriqueItemDto.builder()
                .id(doc.getId())
                .type(type)
                .typeLabel(typeLabel)
                .numero(numero)
                .vehiculeId(vehicule != null ? vehicule.getId() : null)
                .immatriculation(vehicule != null ? vehicule.getImmatriculation() : null)
                .vehiculeMarque(vehicule != null ? vehicule.getMarque() : null)
                .vehiculeModele(vehicule != null ? vehicule.getModele() : null)
                .clientId(client != null ? client.getId() : null)
                .clientNumero(clientNumero)
                .clientNom(client != null ? joinNames(client.getFirstName(), client.getLastName()) : null)
                .clientTelephone(client != null ? client.getPhone() : null)
                .dateCreation(doc.getDateCreation())
                .dateModification(doc.getDateModification())
                .montantHT(montantHT)
                .montantTVA(montantTVA)
                .montantTimbre(montantTimbre)
                .montantTTC(montantTTC)
                .montantTotal(nz(montantTotal))
                .montantPaye(montantPaye)
                .resteAPayer(resteAPayer)
                .statut(statut)
                .ordreReparationId(ordre != null ? ordre.getId() : null)
                .ordreReparationNumero(ordre != null ? ordre.getNumero() : null)
                .remarque(doc.getRemarque())
                .build();
    }

    private Vehicule resolveVehicule(Facturation doc, OrdreReparation ordre, OrdreTravail ordreTravail) {
        if (doc.getVehicule() != null) return doc.getVehicule();
        if (ordre != null && ordre.getVehicule() != null) return ordre.getVehicule();
        if (ordreTravail != null && ordreTravail.getVehicule() != null) return ordreTravail.getVehicule();
        return null;
    }

    private Client resolveClient(Facturation doc, OrdreReparation ordre, OrdreTravail ordreTravail, Vehicule vehicule) {
        if (doc.getClient() != null) return doc.getClient();
        if (ordre != null && ordre.getClient() != null) return ordre.getClient();
        if (ordreTravail != null && ordreTravail.getClient() != null) return ordreTravail.getClient();
        if (vehicule != null && vehicule.getClient() != null) return vehicule.getClient();
        return null;
    }

    private boolean matchesClient(FacturationHistoriqueItemDto item, Long clientId, String clientNumeroLower) {
        if (clientId != null && (item.getClientId() == null || !item.getClientId().equals(clientId))) {
            return false;
        }
        if (clientNumeroLower == null) {
            return true;
        }
        String clientIdStr = item.getClientId() != null ? String.valueOf(item.getClientId()) : null;
        return clientIdStr != null && clientIdStr.equals(clientNumeroLower)
                || containsIgnoreCase(item.getClientNumero(), clientNumeroLower)
                || containsIgnoreCase(item.getClientTelephone(), clientNumeroLower)
                || containsIgnoreCase(item.getClientNom(), clientNumeroLower);
    }

    private boolean matchesKeyword(FacturationHistoriqueItemDto item, String kw) {
        return containsIgnoreCase(item.getNumero(), kw)
                || containsIgnoreCase(item.getTypeLabel(), kw)
                || containsIgnoreCase(item.getImmatriculation(), kw)
                || containsIgnoreCase(item.getVehiculeMarque(), kw)
                || containsIgnoreCase(item.getVehiculeModele(), kw)
                || containsIgnoreCase(item.getClientNumero(), kw)
                || containsIgnoreCase(item.getClientNom(), kw)
                || containsIgnoreCase(item.getClientTelephone(), kw)
                || containsIgnoreCase(item.getStatut(), kw)
                || containsIgnoreCase(item.getOrdreReparationNumero(), kw)
                || containsIgnoreCase(item.getRemarque(), kw);
    }

    private boolean containsIgnoreCase(String value, String kwLower) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(kwLower);
    }

    private String normalize(String s) {
        return (s == null || s.isBlank()) ? null : s.trim().toLowerCase(Locale.ROOT);
    }

    private String joinNames(String firstName, String lastName) {
        String fn = firstName != null ? firstName.trim() : "";
        String ln = lastName != null ? lastName.trim() : "";
        String combined = (fn + " " + ln).trim();
        return combined.isEmpty() ? null : combined;
    }

    private String firstNonBlank(String... candidates) {
        for (String c : candidates) {
            if (c != null && !c.isBlank()) {
                return c.trim();
            }
        }
        return null;
    }

    private BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
