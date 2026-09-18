package sn.oas.facturation.features.clientportal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.service.ClientService;
import sn.oas.facturation.features.clientportal.dto.*;
import sn.oas.facturation.features.devisPrevisionnel.repository.DevisPrevisionnelRepository;
import sn.oas.facturation.features.facturation.data.enums.StatutFacturation;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.garage.repository.GarageRepository;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneReceptionOrdre;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.proforma.repository.ProformaRepository;
import sn.oas.facturation.features.rendezvous.data.enums.RendezVousStatus;
import sn.oas.facturation.features.rendezvous.repository.RendezVousRepository;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientPortalServiceImpl implements ClientPortalService {

    private final ClientService clientService;
    private final VehiculeRepository vehiculeRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final DevisPrevisionnelRepository devisPrevisionnelRepository;
    private final ProformaRepository proformaRepository;
    private final RendezVousRepository rendezVousRepository;
    private final FactureRepository factureRepository;
    private final GarageRepository garageRepository;

    private static final List<StatutOrdreReparation> TERMINATED_STATUSES = List.of(
            StatutOrdreReparation.EN_ATTENTE_PAIEMENT,
            StatutOrdreReparation.TERMINE,
            StatutOrdreReparation.LIVRE
    );

    private record StageInfo(String stage, int stageIndex, String stageLabel, String stageTone, boolean isActive) {}

    private StageInfo resolveStage(StatutOrdreReparation statut) {
        if (statut == null) {
            return new StageInfo("AUCUN_HISTORIQUE", -1, "Aucun historique", "neutral", false);
        }
        return switch (statut) {
            case A_FAIRE -> new StageInfo("A_FAIRE", 0, "Prise en charge", "neutral", true);
            case EN_DIAGNOSTIC -> new StageInfo("EN_DIAGNOSTIC", 1, "Diagnostic", "info", true);
            case EN_ATTENTE_PIECES_MO, EN_ATTENTE_PROFORMA, PROFORMA_VALIDE,
                 EN_ATTENTE_COMMANDE, EN_ATTENTE_SORTIE, EN_ATTENTE_MECANICIEN ->
                    new StageInfo(statut.name(), 2, "Préparation", "pending", true);
            case EN_COURS -> new StageInfo("EN_COURS", 3, "Réparation en cours", "info", true);
            case EN_ATTENTE_PAIEMENT, TERMINE, LIVRE ->
                    new StageInfo(statut.name(), 4, "Terminée", "success", false);
        };
    }

    private List<LigneReceptionItemDTO> mapLignesReception(List<LigneReceptionOrdre> lignes) {
        if (lignes == null || lignes.isEmpty()) {
            return List.of();
        }
        return lignes.stream()
                .map(l -> new LigneReceptionItemDTO(l.getNom(), l.getEtat()))
                .toList();
    }

    private FicheEnCoursSummaryDTO computeFicheEnCours(OrdreReparation derniere) {
        if (derniere == null) {
            return null;
        }

        StageInfo stage = resolveStage(derniere.getStatut());
        boolean shouldDisplay = false;

        if (stage.isActive()) {
            shouldDisplay = true;
        } else if ("Terminée".equals(stage.stageLabel())) {
            if (derniere.getDateSortie() == null) {
                shouldDisplay = true;
            } else {
                Optional<Facture> factureOpt = factureRepository.findFirstByOrdreReparationId(derniere.getId());
                boolean aPaiementPartiel = factureOpt.isPresent()
                        && factureOpt.get().getMontantPaye() != null
                        && factureOpt.get().getMontantPaye().compareTo(BigDecimal.ZERO) > 0;

                if (!aPaiementPartiel) {
                    shouldDisplay = true;
                } else {
                    LocalDateTime lendemainSortie = derniere.getDateSortie().plusDays(1).toLocalDate().atStartOfDay();
                    if (LocalDateTime.now().isBefore(lendemainSortie)) {
                        shouldDisplay = true;
                    }
                }
            }
        }

        if (!shouldDisplay) {
            return null;
        }

        List<FicheEnCoursSummaryDTO.LignePieceSummaryDTO> pieces = (derniere.getLignesOrdreReparationPieces() == null) ? List.of()
                : derniere.getLignesOrdreReparationPieces().stream()
                .map(p -> new FicheEnCoursSummaryDTO.LignePieceSummaryDTO(
                        p.getId(),
                        p.getQuantite(),
                        p.getPrix(),
                        p.getPiece() != null ? new FicheEnCoursSummaryDTO.LignePieceSummaryDTO.PieceRefDTO(p.getPiece().getReference()) : null
                ))
                .toList();

        List<FicheEnCoursSummaryDTO.LigneMOSummaryDTO> mos = (derniere.getLignesOrdreReparationMainDoeuvres() == null) ? List.of()
                : derniere.getLignesOrdreReparationMainDoeuvres().stream()
                .map(m -> new FicheEnCoursSummaryDTO.LigneMOSummaryDTO(
                        m.getId(),
                        m.getNbreHeure(),
                        m.getPrix(),
                        m.getMainDoeuvre() != null ? new FicheEnCoursSummaryDTO.LigneMOSummaryDTO.MORefDTO(m.getMainDoeuvre().getDescription()) : null
                ))
                .toList();

        return new FicheEnCoursSummaryDTO(
                derniere.getId(),
                derniere.getNumero(),
                derniere.getStatut() != null ? derniere.getStatut().name() : null,
                stage.stageLabel(),
                stage.stageTone(),
                derniere.getDateCreation(),
                derniere.getUpdatedAt(),
                derniere.getDateSortie(),
                derniere.getListeDefauts(),
                derniere.getDescriptionTravaux(),
                mapLignesReception(derniere.getLignesReception()),
                pieces,
                mos
        );
    }

    @Override
    public ClientDashboardDTO getDashboard() {
        Client client = clientService.getClientConnecte();
        Long clientId = client.getId();

        long devisEnAttente = devisPrevisionnelRepository.countByClientIdAndStatut(clientId, StatutFacturation.EN_ATTENTE);
        long proformasEnAttente = proformaRepository.countByClientIdAndStatutEnAttente(clientId, StatutFacturation.EN_ATTENTE);
        long rdvAVenir = rendezVousRepository.countByClientIdAndStatutIn(clientId, List.of(RendezVousStatus.EN_ATTENTE, RendezVousStatus.CONFIRME));
        BigDecimal montantDu = factureRepository.sumResteAPayerByClientId(clientId);

        ClientStatsDTO stats = new ClientStatsDTO(devisEnAttente, proformasEnAttente, rdvAVenir, montantDu);

        List<Vehicule> vehicules = vehiculeRepository.findByClientIdAndArchiveParClientFalse(clientId);
        List<ClientDashboardVehiculeDTO> dashboardVehicules = new ArrayList<>();

        for (Vehicule v : vehicules) {
            List<OrdreReparation> ordres = ordreReparationRepository.findByVehiculeIdOrderByDateCreationDesc(v.getId());
            OrdreReparation derniere = ordres.isEmpty() ? null : ordres.get(0);
            StageInfo stage = derniere != null ? resolveStage(derniere.getStatut()) : resolveStage(null);

            dashboardVehicules.add(new ClientDashboardVehiculeDTO(
                    v.getId(),
                    v.getImmatriculation(),
                    v.getMarque(),
                    v.getModele(),
                    v.getAnnee(),
                    v.getKilometrage(),
                    v.getNumeroChassis(),
                    v.getCreatedAt(),
                    stage.stage(),
                    stage.stageIndex(),
                    stage.stageLabel(),
                    stage.stageTone(),
                    derniere != null ? derniere.getNumero() : null,
                    derniere != null ? derniere.getId() : null
            ));
        }

        return new ClientDashboardDTO(stats, dashboardVehicules);
    }

    @Override
    public List<ClientVehiculeCardDTO> getMyVehicules() {
        Client client = clientService.getClientConnecte();
        Long clientId = client.getId();

        List<Vehicule> vehicules = vehiculeRepository.findByClientIdAndArchiveParClientFalse(clientId);
        List<ClientVehiculeCardDTO> result = new ArrayList<>();

        for (Vehicule v : vehicules) {
            List<OrdreReparation> ordres = ordreReparationRepository.findByVehiculeIdOrderByDateCreationDesc(v.getId());
            OrdreReparation derniere = ordres.isEmpty() ? null : ordres.get(0);
            StageInfo stage = derniere != null ? resolveStage(derniere.getStatut()) : resolveStage(null);
            FicheEnCoursSummaryDTO ficheEnCours = computeFicheEnCours(derniere);

            List<ClientInterventionSummaryDTO> historique = ordres.stream()
                    .map(o -> {
                        StageInfo oStage = resolveStage(o.getStatut());
                        return new ClientInterventionSummaryDTO(
                                o.getId(),
                                o.getNumero(),
                                o.getStatut() != null ? o.getStatut().name() : null,
                                oStage.stageLabel(),
                                oStage.stageTone(),
                                o.getDateCreation(),
                                o.getDateSortie(),
                                null,
                                null,
                                List.of()
                        );
                    })
                    .toList();

            result.add(new ClientVehiculeCardDTO(
                    v.getId(),
                    v.getImmatriculation(),
                    v.getMarque(),
                    v.getModele(),
                    v.getAnnee(),
                    v.getKilometrage(),
                    v.getNumeroChassis(),
                    v.getCreatedAt(),
                    stage.stage(),
                    stage.stageIndex(),
                    stage.stageLabel(),
                    stage.stageTone(),
                    stage.isActive(),
                    ficheEnCours,
                    historique
            ));
        }

        return result;
    }

    @Override
    public List<ClientInterventionDTO> getMyInterventions() {
        Client client = clientService.getClientConnecte();
        List<OrdreReparation> ordres = ordreReparationRepository.findByVehiculeClientIdOrderByDateCreationDesc(client.getId());

        return ordres.stream()
                .map(o -> {
                    StageInfo stage = resolveStage(o.getStatut());
                    ClientInterventionDTO.VehiculeInfoDTO vInfo = null;
                    if (o.getVehicule() != null) {
                        vInfo = new ClientInterventionDTO.VehiculeInfoDTO(
                                o.getVehicule().getId(),
                                o.getVehicule().getImmatriculation(),
                                o.getVehicule().getMarque(),
                                o.getVehicule().getModele()
                        );
                    }
                    return new ClientInterventionDTO(
                            o.getId(),
                            o.getNumero(),
                            o.getStatut() != null ? o.getStatut().name() : null,
                            stage.stageLabel(),
                            stage.stageTone(),
                            o.getDateCreation(),
                            o.getDateSortie(),
                            o.getListeDefauts(),
                            o.getDescriptionTravaux(),
                            mapLignesReception(o.getLignesReception()),
                            vInfo
                    );
                })
                .toList();
    }

    @Override
    public List<ClientInterventionSummaryDTO> getVehiculeHistorique(Long vehiculeId) {
        Client client = clientService.getClientConnecte();
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new IllegalArgumentException("Véhicule non trouvé"));

        if (vehicule.getClient() == null || !vehicule.getClient().getId().equals(client.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Accès non autorisé à ce véhicule");
        }

        List<OrdreReparation> ordres = ordreReparationRepository.findByVehiculeIdOrderByDateCreationDesc(vehiculeId);
        return ordres.stream()
                .map(o -> {
                    StageInfo stage = resolveStage(o.getStatut());
                    return new ClientInterventionSummaryDTO(
                            o.getId(),
                            o.getNumero(),
                            o.getStatut() != null ? o.getStatut().name() : null,
                            stage.stageLabel(),
                            stage.stageTone(),
                            o.getDateCreation(),
                            o.getDateSortie(),
                            null,
                            null,
                            List.of()
                    );
                })
                .toList();
    }

    @Override
    public ClientBookingContextDTO getBookingContext() {
        Client client = clientService.getClientConnecte();
        List<Vehicule> vehicules = vehiculeRepository.findByClientIdAndArchiveParClientFalse(client.getId());

        List<ClientBookingContextDTO.VehiculeBookingDTO> vehiculeDTOs = vehicules.stream()
                .map(v -> {
                    boolean hasActiveRepair = ordreReparationRepository
                            .findFirstByVehiculeIdAndStatutNotIn(v.getId(), TERMINATED_STATUSES)
                            .isPresent();
                    return new ClientBookingContextDTO.VehiculeBookingDTO(
                            v.getId(),
                            v.getImmatriculation(),
                            v.getMarque(),
                            v.getModele(),
                            v.getAnnee(),
                            v.getKilometrage(),
                            v.getNumeroChassis(),
                            !hasActiveRepair
                    );
                })
                .toList();

        List<Garage> garages = garageRepository.findByArchivedFalse();
        List<ClientBookingContextDTO.GarageBookingDTO> garageDTOs = garages.stream()
                .map(g -> new ClientBookingContextDTO.GarageBookingDTO(
                        g.getId(),
                        g.getNom(),
                        g.getLocalite(),
                        g.getPrefixe(),
                        g.getNumeroFixe(),
                        g.getNumeroWhatsapp(),
                        g.getEmail()
                ))
                .toList();

        return new ClientBookingContextDTO(vehiculeDTOs, garageDTOs);
    }
}
