package sn.oas.facturation.features.vehicule.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.oas.facturation.features.bonDeLivraison.data.entity.BonDeLivraison;
import sn.oas.facturation.features.bonDeLivraison.repository.BonDeLivraisonRepository;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.bonDeSortie.data.enums.StatutBon;
import sn.oas.facturation.features.bonDeSortie.repository.BonDeSortieRepository;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.devisPrevisionnel.data.entity.DevisPrevisionnel;
import sn.oas.facturation.features.devisPrevisionnel.repository.DevisPrevisionnelRepository;
import sn.oas.facturation.features.facturation.data.enums.StatutFacturation;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.features.noteDePrix.repository.NoteDePrixRepository;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.ordreTravail.data.entity.OrdreTravail;
import sn.oas.facturation.features.ordreTravail.data.enums.StatutOrdreTravail;
import sn.oas.facturation.features.ordreTravail.repository.OrdreTravailRepository;
import sn.oas.facturation.features.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.features.rendezvous.data.enums.RendezVousStatus;
import sn.oas.facturation.features.rendezvous.repository.RendezVousRepository;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.data.enums.TypeHistoriqueVehicule;
import sn.oas.facturation.features.vehicule.dto.VehiculeHistoriqueResponseDto;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehiculeHistoriqueServiceImplTest {

    @Mock VehiculeRepository vehiculeRepository;
    @Mock OrdreReparationRepository ordreReparationRepository;
    @Mock OrdreTravailRepository ordreTravailRepository;
    @Mock FactureRepository factureRepository;
    @Mock NoteDePrixRepository noteDePrixRepository;
    @Mock BonDeSortieRepository bonDeSortieRepository;
    @Mock BonDeLivraisonRepository bonDeLivraisonRepository;
    @Mock DevisPrevisionnelRepository devisPrevisionnelRepository;
    @Mock RendezVousRepository rendezVousRepository;

    @InjectMocks VehiculeHistoriqueServiceImpl service;

    @Test
    void agregeTousLesTypesTrieParDateEtCalculeKpis() {
        Client client = Client.builder().id(13L).firstName("Ousman").lastName("SADJO").phone("+221770000000").build();
        Vehicule vehicule = Vehicule.builder()
                .immatriculation("DK-1234-AA")
                .marque("Toyota")
                .modele("Corolla")
                .annee(2020)
                .kilometrage(125000.0)
                .numeroChassis("CHS-001")
                .actif(true)
                .client(client)
                .build();
        vehicule.setId(5L);
        Agent agent = Agent.builder().id(1L).firstName("Super").lastName("Agent").build();

        OrdreReparation or = OrdreReparation.builder()
                .id(10L)
                .numero("DK-OR-2026-0010")
                .statut(StatutOrdreReparation.FACTURATION)
                .descriptionTravaux("Vidange + freins")
                .dateCreation(LocalDateTime.of(2026, 11, 10, 9, 0))
                .vehicule(vehicule)
                .build();

        OrdreTravail ot = OrdreTravail.builder()
                .id(15L)
                .numero("DK-OT-2026-0015")
                .statut(StatutOrdreTravail.TERMINE)
                .rapportReparation("Vidange effectuée")
                .kilometrageSortie(125100.0)
                .createdAt(LocalDateTime.of(2026, 11, 10, 12, 0))
                .vehicule(vehicule)
                .ordreReparation(or)
                .build();

        Facture facture = Facture.builder()
                .id(20L)
                .numero("DK-FC-2026-0020")
                .dateCreation(LocalDateTime.of(2026, 11, 12, 14, 0))
                .montantTotal(new BigDecimal("150000"))
                .montantPaye(new BigDecimal("100000"))
                .resteAPayer(new BigDecimal("50000"))
                .statutPaiement(StatutPaiement.PARTIEL)
                .kilometrage(125000.0)
                .ordreReparation(or)
                .agent(agent)
                .build();

        NoteDePrix note = NoteDePrix.builder()
                .id(30L)
                .numero("DK-NP-2026-0030")
                .dateCreation(LocalDateTime.of(2026, 11, 13, 16, 0))
                .montantHT(new BigDecimal("25000"))
                .montantTotal(new BigDecimal("25000"))
                .montantPaye(BigDecimal.ZERO)
                .resteAPayer(new BigDecimal("25000"))
                .statutPaiement(StatutPaiement.NON_PAYE)
                .build();

        BonDeSortie bs = BonDeSortie.builder()
                .id(40L)
                .reference("BS-2026-0040")
                .date(LocalDateTime.of(2026, 11, 11, 10, 0))
                .statut(StatutBon.VALIDE)
                .montantTotal(new BigDecimal("80000"))
                .ordreReparation(or)
                .agentEmetteur(agent)
                .build();

        BonDeLivraison bl = BonDeLivraison.builder()
                .id(50L)
                .numero("DK-BL-2026-0050")
                .dateCreation(LocalDateTime.of(2026, 11, 11, 15, 0))
                .montantTTC(new BigDecimal("150000"))
                .ordreReparation(or)
                .facture(facture)
                .agent(agent)
                .build();

        DevisPrevisionnel devis = DevisPrevisionnel.builder()
                .id(60L)
                .numero("DK-DEV-2026-0060")
                .dateCreation(LocalDateTime.of(2026, 11, 9, 11, 0))
                .statut(StatutFacturation.ACCEPTE)
                .notesReparation("Changement plaquettes")
                .montantTotal(new BigDecimal("140000"))
                .kilometrageVehicule(124500.0)
                .agent(agent)
                .vehicule(vehicule)
                .client(client)
                .build();

        RendezVous rdv = RendezVous.builder()
                .id(70L)
                .numero("RDV-2026-0070")
                .dateRendezVous(LocalDateTime.of(2026, 11, 8, 8, 30))
                .statut(RendezVousStatus.CONFIRME)
                .motif("Révision")
                .vehicule(vehicule)
                .client(client)
                .build();

        when(vehiculeRepository.findById(5L)).thenReturn(Optional.of(vehicule));
        when(ordreReparationRepository.findByVehiculeIdOrderByDateCreationDesc(5L)).thenReturn(List.of(or));
        when(ordreTravailRepository.findHistoriqueByVehiculeId(5L)).thenReturn(List.of(ot));
        when(factureRepository.findHistoriqueByVehiculeId(5L)).thenReturn(List.of(facture));
        when(noteDePrixRepository.findHistoriqueByVehiculeId(5L)).thenReturn(List.of(note));
        when(bonDeSortieRepository.findHistoriqueByVehiculeId(5L)).thenReturn(List.of(bs));
        when(bonDeLivraisonRepository.findHistoriqueByVehiculeId(5L)).thenReturn(List.of(bl));
        when(devisPrevisionnelRepository.findByVehiculeIdOrderByDateCreationDesc(5L)).thenReturn(List.of(devis));
        when(rendezVousRepository.findByVehiculeIdOrderByDateRendezVousDesc(5L)).thenReturn(List.of(rdv));

        VehiculeHistoriqueResponseDto dto = service.getHistorique(5L, null, null, null, null, null, null);

        assertEquals(5L, dto.getVehiculeId());
        assertEquals("Ousman SADJO", dto.getClientNom());
        assertEquals(8L, dto.getTotalOperations());
        assertEquals(1L, dto.getTotalOrdresReparation());
        assertEquals(1L, dto.getTotalOrdresTravail());
        assertEquals(1L, dto.getTotalFactures());
        assertEquals(1L, dto.getTotalNotesDePrix());
        assertEquals(1L, dto.getTotalBonsSortie());
        assertEquals(1L, dto.getTotalBonsLivraison());
        assertEquals(1L, dto.getTotalDevis());
        assertEquals(1L, dto.getTotalRendezVous());
        assertEquals(new BigDecimal("175000"), dto.getMontantTotalFacture());
        assertEquals(new BigDecimal("100000"), dto.getMontantTotalPaye());
        assertEquals(new BigDecimal("75000"), dto.getResteAPayer());

        assertEquals(8, dto.getEvenements().size());
        assertEquals(TypeHistoriqueVehicule.NOTE_PRIX, dto.getEvenements().get(0).getType());
        assertEquals(TypeHistoriqueVehicule.FACTURE, dto.getEvenements().get(1).getType());
        assertEquals(TypeHistoriqueVehicule.RDV, dto.getEvenements().get(7).getType());

        // Vérification filtrage par type + pagination tout en conservant les KPIs globaux
        VehiculeHistoriqueResponseDto filtered = service.getHistorique(
                5L, TypeHistoriqueVehicule.FACTURE, "0020",
                LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 12), 0, 5);
        assertEquals(8L, filtered.getTotalOperations());
        assertEquals(1L, filtered.getTotalElements());
        assertEquals(1, filtered.getEvenements().size());
        assertEquals("DK-FC-2026-0020", filtered.getEvenements().get(0).getNumero());

        VehiculeHistoriqueResponseDto filteredOt = service.getHistorique(
                5L, TypeHistoriqueVehicule.OT, null, null, null, null, null);
        assertEquals(1L, filteredOt.getTotalElements());
        assertEquals("DK-OT-2026-0015", filteredOt.getEvenements().get(0).getNumero());
        assertEquals("Ordre de travail", filteredOt.getEvenements().get(0).getTypeLabel());
    }

    @Test
    void rejetteVehiculeInconnuEtPlageDatesInvalide() {
        assertThrows(BadRequestException.class, () ->
                service.getHistorique(5L, null, null, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 10), null, null));

        when(vehiculeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () ->
                service.getHistorique(999L, null, null, null, null, null, null));
    }
}
