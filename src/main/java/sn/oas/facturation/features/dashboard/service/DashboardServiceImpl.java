package sn.oas.facturation.features.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.bonDeSortie.data.enums.StatutBon;
import sn.oas.facturation.features.bonDeSortie.repository.BonDeSortieRepository;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.dashboard.dto.response.*;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.piecedetache.service.AlerteService;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ClientRepository clientRepository;
    private final VehiculeRepository vehiculeRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final BonDeSortieRepository bonDeSortieRepository;
    private final AlerteService alerteService;

    @Override
    @Transactional(readOnly = true)
    public DashboardSuperAgentResponseDTO getSuperAgentDashboard() {
        long totalClients = clientRepository.count();
        long totalVehicules = vehiculeRepository.count();
        long totalRupturesDeStock = alerteService.getRuptures().size();
        long totalBonsEnAttente = bonDeSortieRepository.countByStatut(StatutBon.EN_ATTENTE);

        EtatOrdreReparationDTO etatOrdres = buildEtatOrdresReparation();

        // Ordres de réparation récents (Top 5)
        List<OrdreReparationRecentDTO> ordresRecents = ordreReparationRepository.findTop5ByOrderByIdDesc()
                .stream()
                .map(o -> OrdreReparationRecentDTO.builder()
                        .id(o.getId())
                        .numero(o.getNumero())
                        .statut(o.getStatut() != null ? o.getStatut().name() : null)
                        .immatriculation(o.getVehicule() != null ? o.getVehicule().getImmatriculation() : null)
                        .date(o.getDateCreation())
                        .build())
                .toList();

        // Clients récents (Top 5)
        List<ClientRecentDTO> clientsRecents = buildClientsRecents();

        // Alertes stock récentes (Top 5)
        List<AlerteStockRecentDTO> alertesStock = alerteService.getAlertes()
                .stream()
                .limit(5)
                .map(a -> AlerteStockRecentDTO.builder()
                        .id(a.pieceId())
                        .designation(a.reference())
                        .reference(a.reference())
                        .stockMagasin(a.stockMagasin())
                        .statut(a.typeAlerte() != null ? a.typeAlerte().name() : null)
                        .build())
                .toList();

        return DashboardSuperAgentResponseDTO.builder()
                .totalClients(totalClients)
                .totalVehicules(totalVehicules)
                .totalRupturesDeStock(totalRupturesDeStock)
                .totalBonsDeSortieEnAttente(totalBonsEnAttente)
                .etatOrdresReparation(etatOrdres)
                .ordresRecents(ordresRecents)
                .clientsRecents(clientsRecents)
                .alertesStock(alertesStock)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardAgentResponse getAgentDashboard() {
        long totalClients = clientRepository.count();
        long totalVehicules = vehiculeRepository.count();
        long totalBonsEnAttente = bonDeSortieRepository.countByStatut(StatutBon.EN_ATTENTE);

        EtatOrdreReparationDTO etatOrdres = buildEtatOrdresReparation();
        List<ClientRecentDTO> clientsRecents = buildClientsRecents();
        List<BonDeSortieEnAttenteDTO> bonsEnAttente = buildBonsEnAttenteValidation();

        return DashboardAgentResponse.builder()
                .totalClients(totalClients)
                .totalVehicules(totalVehicules)
                .totalBonsDeSortieEnAttente(totalBonsEnAttente)
                .etatOrdresReparation(etatOrdres)
                .clientsRecents(clientsRecents)
                .bonsDeSortieEnAttente(bonsEnAttente)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardChefAtelierResponse getChefAtelierDashboard() {
        long totalBonsDeSortieEnAttente = bonDeSortieRepository.countByStatut(StatutBon.EN_ATTENTE);
        long totalVehicules = vehiculeRepository.count();

        EtatOrdreReparationDTO etatOrdres = buildEtatOrdresReparation();
        List<BonDeSortieEnAttenteDTO> bonsEnAttente = buildBonsEnAttenteValidation();

        return DashboardChefAtelierResponse.builder()
                .totalBonsDeSortieEnAttente(totalBonsDeSortieEnAttente)
                .totalVehicules(totalVehicules)
                .etatOrdresReparation(etatOrdres)
                .bonsDeSortieEnAttenteValidation(bonsEnAttente)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardAgentMagasinResponse getAgentMagasinDashboard() {
        long totalAlertes = alerteService.getAlertes().size();
        long totalRuptures = alerteService.getRuptures().size();
        long totalStocksFaibles = alerteService.getStocksFaibles().size();
        long totalBonsEnAttente = bonDeSortieRepository.countByStatut(StatutBon.EN_ATTENTE);

        List<AlerteStockRecentDTO> rupturesDeStock = alerteService.getRuptures()
                .stream()
                .limit(5)
                .map(a -> AlerteStockRecentDTO.builder()
                        .id(a.pieceId())
                        .designation(a.reference())
                        .reference(a.reference())
                        .seuil(a.seuilApplique())
                        .stockMagasin(a.stockMagasin())
                        .statut(a.typeAlerte() != null ? a.typeAlerte().name() : null)
                        .build())
                .toList();

        List<AlerteStockRecentDTO> stocksFaibles = alerteService.getStocksFaibles()
                .stream()
                .limit(5)
                .map(a -> AlerteStockRecentDTO.builder()
                        .id(a.pieceId())
                        .designation(a.reference())
                        .reference(a.reference())
                        .seuil(a.seuilApplique())
                        .stockMagasin(a.stockMagasin())
                        .statut(a.typeAlerte() != null ? a.typeAlerte().name() : null)
                        .build())
                .toList();

        return DashboardAgentMagasinResponse.builder()
                .totalAlertes(totalAlertes)
                .totalRuptures(totalRuptures)
                .totalStocksFaibles(totalStocksFaibles)
                .totalBonsEnAttente(totalBonsEnAttente)
                .rupturesDeStock(rupturesDeStock)
                .stocksFaibles(stocksFaibles)
                .build();
    }

    private EtatOrdreReparationDTO buildEtatOrdresReparation() {
        long diagnostic = ordreReparationRepository.countByStatut(StatutOrdreReparation.EN_DIAGNOSTIC)
                + ordreReparationRepository.countByStatut(StatutOrdreReparation.A_FAIRE);
        long attenteProforma = ordreReparationRepository.countByStatut(StatutOrdreReparation.EN_ATTENTE_PROFORMA);
        long proformaValide = ordreReparationRepository.countByStatut(StatutOrdreReparation.PROFORMA_VALIDE);
        long attentePieces = ordreReparationRepository.countByStatut(StatutOrdreReparation.EN_ATTENTE_COMMANDE);
        long attenteSortie = ordreReparationRepository.countByStatut(StatutOrdreReparation.EN_ATTENTE_SORTIE);
        long enReparation = ordreReparationRepository.countByStatut(StatutOrdreReparation.EN_COURS)
                + ordreReparationRepository.countByStatut(StatutOrdreReparation.EN_ATTENTE_MECANICIEN);
        long attentePaiement = ordreReparationRepository.countByStatut(StatutOrdreReparation.EN_ATTENTE_PAIEMENT);
        long termine = ordreReparationRepository.countByStatut(StatutOrdreReparation.TERMINE)
                + ordreReparationRepository.countByStatut(StatutOrdreReparation.LIVRE);
        long totalActifs = ordreReparationRepository.countByStatutNotIn(List.of(StatutOrdreReparation.TERMINE, StatutOrdreReparation.LIVRE));

        return EtatOrdreReparationDTO.builder()
                .diagnostic(diagnostic)
                .attenteProforma(attenteProforma)
                .proformaValide(proformaValide)
                .attentePieces(attentePieces)
                .attenteSortie(attenteSortie)
                .enReparation(enReparation)
                .attentePaiement(attentePaiement)
                .termine(termine)
                .totalActifs(totalActifs)
                .build();
    }

    private List<ClientRecentDTO> buildClientsRecents() {
        return clientRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(c -> ClientRecentDTO.builder()
                        .id(c.getId())
                        .nom((c.getFirstName() != null ? c.getFirstName() + " " : "") + (c.getLastName() != null ? c.getLastName() : ""))
                        .telephone(c.getPhone())
                        .build())
                .toList();
    }

    private List<BonDeSortieEnAttenteDTO> buildBonsEnAttenteValidation() {
        return bonDeSortieRepository.findByStatutOrderByDateDesc(StatutBon.EN_ATTENTE)
                .stream()
                .limit(5)
                .map(b -> BonDeSortieEnAttenteDTO.builder()
                        .id(b.getId())
                        .reference(b.getReference())
                        .immatriculation(b.getVehicule() != null ? b.getVehicule().getImmatriculation() : null)
                        .clientNom(b.getClient() != null ? (b.getClient().getFirstName() != null ? b.getClient().getFirstName() + " " : "") + (b.getClient().getLastName() != null ? b.getClient().getLastName() : "") : null)
                        .nombrePieces(b.getLignesBonDeSortiePieces() != null ? b.getLignesBonDeSortiePieces().size() : 0)
                        .date(b.getDate())
                        .build())
                .toList();
    }
}
