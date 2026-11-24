package sn.oas.facturation.features.vehicule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.bonDeLivraison.data.entity.BonDeLivraison;
import sn.oas.facturation.features.bonDeLivraison.repository.BonDeLivraisonRepository;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.bonDeSortie.repository.BonDeSortieRepository;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.devisPrevisionnel.data.entity.DevisPrevisionnel;
import sn.oas.facturation.features.devisPrevisionnel.repository.DevisPrevisionnelRepository;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.features.noteDePrix.repository.NoteDePrixRepository;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.ordreTravail.data.entity.OrdreTravail;
import sn.oas.facturation.features.ordreTravail.repository.OrdreTravailRepository;
import sn.oas.facturation.features.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.features.rendezvous.repository.RendezVousRepository;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.data.enums.TypeHistoriqueVehicule;
import sn.oas.facturation.features.vehicule.dto.VehiculeHistoriqueItemDto;
import sn.oas.facturation.features.vehicule.dto.VehiculeHistoriqueResponseDto;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehiculeHistoriqueServiceImpl implements VehiculeHistoriqueService {

    private static final int MAX_PAGE_SIZE = 500;

    private final VehiculeRepository vehiculeRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final OrdreTravailRepository ordreTravailRepository;
    private final FactureRepository factureRepository;
    private final NoteDePrixRepository noteDePrixRepository;
    private final BonDeSortieRepository bonDeSortieRepository;
    private final BonDeLivraisonRepository bonDeLivraisonRepository;
    private final DevisPrevisionnelRepository devisPrevisionnelRepository;
    private final RendezVousRepository rendezVousRepository;

    @Override
    @Transactional(readOnly = true)
    public VehiculeHistoriqueResponseDto getHistorique(
            Long vehiculeId,
            TypeHistoriqueVehicule type,
            String keyword,
            LocalDate dateDebut,
            LocalDate dateFin,
            Integer page,
            Integer size) {

        if (dateDebut != null && dateFin != null && dateDebut.isAfter(dateFin)) {
            throw new BadRequestException("La date de début doit être antérieure ou égale à la date de fin.");
        }

        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'id : " + vehiculeId));

        List<OrdreReparation> ordres = ordreReparationRepository.findByVehiculeIdOrderByDateCreationDesc(vehiculeId);
        List<OrdreTravail> ordresTravail = ordreTravailRepository.findHistoriqueByVehiculeId(vehiculeId);
        List<Facture> factures = factureRepository.findHistoriqueByVehiculeId(vehiculeId);
        List<NoteDePrix> notesDePrix = noteDePrixRepository.findHistoriqueByVehiculeId(vehiculeId);
        List<BonDeSortie> bonsSortie = bonDeSortieRepository.findHistoriqueByVehiculeId(vehiculeId);
        List<BonDeLivraison> bonsLivraison = bonDeLivraisonRepository.findHistoriqueByVehiculeId(vehiculeId);
        List<DevisPrevisionnel> devisList = devisPrevisionnelRepository.findByVehiculeIdOrderByDateCreationDesc(vehiculeId);
        List<RendezVous> rendezVousList = rendezVousRepository.findByVehiculeIdOrderByDateRendezVousDesc(vehiculeId);

        List<VehiculeHistoriqueItemDto> allEvents = new ArrayList<>();
        ordres.forEach(o -> allEvents.add(mapOrdre(o)));
        ordresTravail.forEach(ot -> allEvents.add(mapOrdreTravail(ot)));
        factures.forEach(f -> allEvents.add(mapFacture(f)));
        notesDePrix.forEach(n -> allEvents.add(mapNoteDePrix(n)));
        bonsSortie.forEach(bs -> allEvents.add(mapBonSortie(bs)));
        bonsLivraison.forEach(bl -> allEvents.add(mapBonLivraison(bl)));
        devisList.forEach(d -> allEvents.add(mapDevis(d)));
        rendezVousList.forEach(r -> allEvents.add(mapRendezVous(r)));

        allEvents.sort(Comparator
                .comparing(VehiculeHistoriqueItemDto::getDate, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(VehiculeHistoriqueItemDto::getId, Comparator.nullsLast(Comparator.reverseOrder())));

        BigDecimal montantTotalFacture = BigDecimal.ZERO;
        BigDecimal montantTotalPaye = BigDecimal.ZERO;
        BigDecimal resteAPayer = BigDecimal.ZERO;
        for (VehiculeHistoriqueItemDto item : allEvents) {
            if (item.getType() == TypeHistoriqueVehicule.FACTURE || item.getType() == TypeHistoriqueVehicule.NOTE_PRIX) {
                montantTotalFacture = montantTotalFacture.add(nz(item.getMontant()));
                montantTotalPaye = montantTotalPaye.add(nz(item.getMontantPaye()));
                resteAPayer = resteAPayer.add(nz(item.getResteAPayer()));
            }
        }

        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim().toLowerCase(Locale.ROOT);
        List<VehiculeHistoriqueItemDto> filtered = allEvents.stream()
                .filter(item -> type == null || item.getType() == type)
                .filter(item -> kw == null || matchesKeyword(item, kw))
                .filter(item -> matchesDateRange(item, dateDebut, dateFin))
                .toList();

        long totalElements = filtered.size();
        Integer effectivePage = null;
        Integer effectiveSize = null;
        int totalPages;
        List<VehiculeHistoriqueItemDto> resultEvents;

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

        Client client = vehicule.getClient();
        return VehiculeHistoriqueResponseDto.builder()
                .vehiculeId(vehicule.getId())
                .immatriculation(vehicule.getImmatriculation())
                .marque(vehicule.getMarque())
                .modele(vehicule.getModele())
                .annee(vehicule.getAnnee())
                .kilometrage(vehicule.getKilometrage())
                .numeroChassis(vehicule.getNumeroChassis())
                .actif(vehicule.isActif())
                .clientId(client != null ? client.getId() : null)
                .clientNom(joinNames(client != null ? client.getFirstName() : null, client != null ? client.getLastName() : null))
                .clientTelephone(client != null ? client.getPhone() : null)
                .totalOperations(allEvents.size())
                .totalOrdresReparation(ordres.size())
                .totalOrdresTravail(ordresTravail.size())
                .totalFactures(factures.size())
                .totalNotesDePrix(notesDePrix.size())
                .totalBonsSortie(bonsSortie.size())
                .totalBonsLivraison(bonsLivraison.size())
                .totalDevis(devisList.size())
                .totalRendezVous(rendezVousList.size())
                .montantTotalFacture(montantTotalFacture)
                .montantTotalPaye(montantTotalPaye)
                .resteAPayer(resteAPayer)
                .page(effectivePage)
                .size(effectiveSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .evenements(resultEvents)
                .build();
    }

    private VehiculeHistoriqueItemDto mapOrdre(OrdreReparation o) {
        int nbPieces = o.getLignesOrdreReparationPieces() != null ? o.getLignesOrdreReparationPieces().size() : 0;
        int nbMo = o.getLignesOrdreReparationMainDoeuvres() != null ? o.getLignesOrdreReparationMainDoeuvres().size() : 0;
        List<String> metaParts = new ArrayList<>();
        if (nbPieces > 0) metaParts.add(nbPieces + " pièce(s)");
        if (nbMo > 0) metaParts.add(nbMo + " MO");
        if (o.getDateSortie() != null) metaParts.add("Sortie: " + o.getDateSortie().toLocalDate());

        return VehiculeHistoriqueItemDto.builder()
                .id(o.getId())
                .type(TypeHistoriqueVehicule.OR)
                .typeLabel("Ordre de réparation")
                .numero(firstNonBlank(o.getNumero(), "OR #" + o.getId()))
                .date(o.getDateCreation() != null ? o.getDateCreation() : o.getUpdatedAt())
                .statut(o.getStatut() != null ? o.getStatut().name() : "RECEPTION")
                .description(firstNonBlank(o.getDescriptionTravaux(), o.getListeDefauts(), "Intervention atelier"))
                .meta(metaParts.isEmpty() ? null : String.join(" · ", metaParts))
                .ordreReparationId(o.getId())
                .ordreReparationNumero(o.getNumero())
                .build();
    }

    private VehiculeHistoriqueItemDto mapOrdreTravail(OrdreTravail ot) {
        int nbTechs = ot.getLignesTechniciens() != null ? ot.getLignesTechniciens().size() : 0;
        List<String> metaParts = new ArrayList<>();
        if (nbTechs > 0) metaParts.add(nbTechs + " technicien(s)");
        if (ot.getDateSortie() != null) {
            metaParts.add("Sortie: " + ot.getDateSortie().toLocalDate());
        } else if (ot.getDateFinReparation() != null) {
            metaParts.add("Fin: " + ot.getDateFinReparation().toLocalDate());
        }
        String km = formatKm(ot.getKilometrageSortie());
        if (km != null) metaParts.add(km);

        OrdreReparation ordre = ot.getOrdreReparation();
        String note = firstNonBlank(ot.getRapportReparation(), ot.getRemarquesPreparation(), ot.getRemarquesClient());
        String description = buildOrDescription(ordre, note, "Ordre de travail");

        return VehiculeHistoriqueItemDto.builder()
                .id(ot.getId())
                .type(TypeHistoriqueVehicule.OT)
                .typeLabel("Ordre de travail")
                .numero(firstNonBlank(ot.getNumero(), "OT #" + ot.getId()))
                .date(ot.getCreatedAt() != null ? ot.getCreatedAt() : ot.getUpdatedAt())
                .statut(ot.getStatut() != null ? ot.getStatut().name() : "RECAPITULATIF")
                .description(description)
                .meta(metaParts.isEmpty() ? null : String.join(" · ", metaParts))
                .ordreReparationId(ordre != null ? ordre.getId() : null)
                .ordreReparationNumero(ordre != null ? ordre.getNumero() : null)
                .kilometrage(ot.getKilometrageSortie())
                .build();
    }

    private VehiculeHistoriqueItemDto mapFacture(Facture f) {
        BigDecimal montant = nz(f.getMontantTotal() != null ? f.getMontantTotal() : f.getMontantTTC());
        BigDecimal paye = nz(f.getMontantPaye());
        BigDecimal reste = f.getResteAPayer() != null ? f.getResteAPayer() : montant.subtract(paye).max(BigDecimal.ZERO);
        OrdreReparation ordre = f.getOrdreReparation();
        String description = buildOrDescription(ordre, f.getRemarque(), "Facture client");

        return buildItemWithAgent(
                f.getId(),
                TypeHistoriqueVehicule.FACTURE,
                "Facture",
                firstNonBlank(f.getNumero(), "FC #" + f.getId()),
                f.getDateCreation(),
                f.getStatutPaiement() != null ? f.getStatutPaiement().name() : "NON_PAYE",
                description,
                formatKm(f.getKilometrage()),
                montant,
                paye,
                reste,
                ordre,
                f.getAgent(),
                f.getKilometrage());
    }

    private VehiculeHistoriqueItemDto mapNoteDePrix(NoteDePrix n) {
        BigDecimal montant = nz(n.getMontantTotal() != null ? n.getMontantTotal() : n.getMontantHT());
        BigDecimal paye = nz(n.getMontantPaye());
        BigDecimal reste = n.getResteAPayer() != null ? n.getResteAPayer() : montant.subtract(paye).max(BigDecimal.ZERO);
        OrdreReparation ordre = n.getOrdreReparation();
        String description = buildOrDescription(ordre, n.getRemarque(), "Note de prix (HT)");
        String statut = n.getStatutPaiement() != null
                ? n.getStatutPaiement().name()
                : (n.getStatut() != null ? n.getStatut().name() : "NON_PAYE");

        return buildItemWithAgent(
                n.getId(),
                TypeHistoriqueVehicule.NOTE_PRIX,
                "Note de prix",
                firstNonBlank(n.getNumero(), "NP #" + n.getId()),
                n.getDateCreation(),
                statut,
                description,
                formatKm(n.getKilometrage()),
                montant,
                paye,
                reste,
                ordre,
                n.getAgent(),
                n.getKilometrage());
    }

    private VehiculeHistoriqueItemDto mapBonSortie(BonDeSortie bs) {
        int nbPieces = bs.getLignesBonDeSortiePieces() != null ? bs.getLignesBonDeSortiePieces().size() : 0;
        int nbMo = bs.getLignesBonDeSortieMainDoeuvres() != null ? bs.getLignesBonDeSortieMainDoeuvres().size() : 0;
        OrdreReparation ordre = bs.getOrdreReparation();

        List<String> metaParts = new ArrayList<>();
        if (ordre != null && ordre.getNumero() != null && !ordre.getNumero().isBlank()) {
            metaParts.add(ordre.getNumero());
        }
        if (nbPieces > 0) metaParts.add(nbPieces + " pièce(s)");
        if (nbMo > 0) metaParts.add(nbMo + " MO");

        BigDecimal montant = bs.getMontantTTC() != null ? bs.getMontantTTC() : bs.getMontantTotal();
        return buildItemWithAgent(
                bs.getId(),
                TypeHistoriqueVehicule.BS,
                "Bon de sortie",
                firstNonBlank(bs.getReference(), "BS #" + bs.getId()),
                bs.getDate() != null ? bs.getDate() : bs.getDateValidation(),
                bs.getStatut() != null ? bs.getStatut().name() : "EN_ATTENTE",
                firstNonBlank(bs.getRemarque(), "Sortie de pièces / main-d’œuvre"),
                metaParts.isEmpty() ? null : String.join(" · ", metaParts),
                montant,
                null,
                null,
                ordre,
                bs.getAgentEmetteur(),
                null);
    }

    private VehiculeHistoriqueItemDto mapBonLivraison(BonDeLivraison bl) {
        OrdreReparation ordre = bl.getOrdreReparation();
        String linkedDoc = bl.getFacture() != null
                ? bl.getFacture().getNumero()
                : (bl.getNoteDePrix() != null ? bl.getNoteDePrix().getNumero() : null);

        String statut = bl.getFacture() != null
                ? "FACTURÉ"
                : (bl.getNoteDePrix() != null ? "NOTE DE PRIX" : "CRÉÉ");

        String meta = null;
        if (linkedDoc != null && !linkedDoc.isBlank()) {
            meta = "Document: " + linkedDoc;
        } else if (bl.getBonsDeSortie() != null && !bl.getBonsDeSortie().isEmpty()) {
            String refs = bl.getBonsDeSortie().stream()
                    .map(BonDeSortie::getReference)
                    .filter(r -> r != null && !r.isBlank())
                    .collect(Collectors.joining(", "));
            if (!refs.isBlank()) meta = refs;
        }

        String description = ordre != null && ordre.getNumero() != null
                ? "OR " + ordre.getNumero() + (bl.getRemarque() != null && !bl.getRemarque().isBlank() ? " — " + bl.getRemarque().trim() : "")
                : firstNonBlank(bl.getRemarque(), "Bon de livraison");

        return buildItemWithAgent(
                bl.getId(),
                TypeHistoriqueVehicule.BL,
                "Bon de livraison",
                firstNonBlank(bl.getNumero(), "BL #" + bl.getId()),
                bl.getDateCreation(),
                statut,
                description,
                meta,
                bl.getMontantTTC() != null ? bl.getMontantTTC() : bl.getMontantHT(),
                null,
                null,
                ordre,
                bl.getAgent(),
                bl.getKilometrage());
    }

    private VehiculeHistoriqueItemDto mapDevis(DevisPrevisionnel d) {
        return buildItemWithAgent(
                d.getId(),
                TypeHistoriqueVehicule.DEVIS,
                "Devis prévisionnel",
                firstNonBlank(d.getNumero(), "DEV #" + d.getId()),
                d.getDateCreation() != null ? d.getDateCreation() : d.getUpdatedAt(),
                d.getStatut() != null ? d.getStatut().name() : "EN_ATTENTE",
                firstNonBlank(d.getNotesReparation(), "Devis prévisionnel"),
                formatKm(d.getKilometrageVehicule()),
                d.getMontantTotal(),
                null,
                null,
                d.getOrdreReparation(),
                d.getAgent(),
                d.getKilometrageVehicule());
    }

    private VehiculeHistoriqueItemDto mapRendezVous(RendezVous r) {
        return VehiculeHistoriqueItemDto.builder()
                .id(r.getId())
                .type(TypeHistoriqueVehicule.RDV)
                .typeLabel("Rendez-vous")
                .numero(firstNonBlank(r.getNumero(), "RDV #" + r.getId()))
                .date(r.getDateRendezVous() != null ? r.getDateRendezVous() : r.getDateCreation())
                .statut(r.getStatut() != null ? r.getStatut().name() : "EN_ATTENTE")
                .description(firstNonBlank(r.getMotif(), r.getCommentaire(), "Rendez-vous client"))
                .build();
    }

    private VehiculeHistoriqueItemDto buildItemWithAgent(
            Long id,
            TypeHistoriqueVehicule type,
            String typeLabel,
            String numero,
            java.time.LocalDateTime date,
            String statut,
            String description,
            String meta,
            BigDecimal montant,
            BigDecimal montantPaye,
            BigDecimal resteAPayer,
            OrdreReparation ordre,
            Agent agent,
            Double kilometrage) {
        return VehiculeHistoriqueItemDto.builder()
                .id(id)
                .type(type)
                .typeLabel(typeLabel)
                .numero(numero)
                .date(date)
                .statut(statut)
                .description(description)
                .meta(meta)
                .montant(montant)
                .montantPaye(montantPaye)
                .resteAPayer(resteAPayer)
                .ordreReparationId(ordre != null ? ordre.getId() : null)
                .ordreReparationNumero(ordre != null ? ordre.getNumero() : null)
                .agentId(agent != null ? agent.getId() : null)
                .agentNom(agent != null ? joinNames(agent.getFirstName(), agent.getLastName()) : null)
                .kilometrage(kilometrage)
                .build();
    }

    private boolean matchesKeyword(VehiculeHistoriqueItemDto item, String kw) {
        return containsIgnoreCase(item.getNumero(), kw)
                || containsIgnoreCase(item.getTypeLabel(), kw)
                || containsIgnoreCase(item.getDescription(), kw)
                || containsIgnoreCase(item.getStatut(), kw)
                || containsIgnoreCase(item.getMeta(), kw)
                || containsIgnoreCase(item.getOrdreReparationNumero(), kw)
                || containsIgnoreCase(item.getAgentNom(), kw);
    }

    private boolean matchesDateRange(VehiculeHistoriqueItemDto item, LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut == null && dateFin == null) {
            return true;
        }
        if (item.getDate() == null) {
            return false;
        }
        LocalDate d = item.getDate().toLocalDate();
        if (dateDebut != null && d.isBefore(dateDebut)) {
            return false;
        }
        return dateFin == null || !d.isAfter(dateFin);
    }

    private boolean containsIgnoreCase(String value, String kwLower) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(kwLower);
    }

    private String buildOrDescription(OrdreReparation ordre, String remarque, String fallback) {
        String rem = (remarque != null && !remarque.isBlank()) ? remarque.trim() : null;
        if (ordre != null && ordre.getNumero() != null && !ordre.getNumero().isBlank()) {
            return "Liée à " + ordre.getNumero() + (rem != null ? " — " + rem : "");
        }
        return rem != null ? rem : fallback;
    }

    private String formatKm(Double km) {
        if (km == null || km <= 0) return null;
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.FRANCE);
        return nf.format(Math.round(km)) + " km";
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
