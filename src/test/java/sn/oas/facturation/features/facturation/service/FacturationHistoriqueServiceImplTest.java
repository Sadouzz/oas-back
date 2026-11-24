package sn.oas.facturation.features.facturation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.oas.facturation.features.avoirHT.data.entity.AvoirHT;
import sn.oas.facturation.features.avoirHT.repository.AvoirHTRepository;
import sn.oas.facturation.features.avoirTTC.data.entity.AvoirTTC;
import sn.oas.facturation.features.avoirTTC.repository.AvoirTTCRepository;
import sn.oas.facturation.features.bonDeLivraison.data.entity.BonDeLivraison;
import sn.oas.facturation.features.bonDeLivraison.repository.BonDeLivraisonRepository;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.facturation.data.enums.StatutFacturation;
import sn.oas.facturation.features.facturation.data.enums.TypeHistoriqueFacturation;
import sn.oas.facturation.features.facturation.dto.FacturationHistoriqueResponseDto;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.features.noteDePrix.repository.NoteDePrixRepository;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.shared.exception.BadRequestException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturationHistoriqueServiceImplTest {

    @Mock BonDeLivraisonRepository bonDeLivraisonRepository;
    @Mock FactureRepository factureRepository;
    @Mock AvoirTTCRepository avoirTTCRepository;
    @Mock NoteDePrixRepository noteDePrixRepository;
    @Mock AvoirHTRepository avoirHTRepository;

    @InjectMocks FacturationHistoriqueServiceImpl service;

    @Test
    void agregeLesCinqTypesFiltreClientEtCalculeTotaux() {
        Client client1 = Client.builder()
                .id(1667L)
                .codeClient("CL-1667")
                .firstName("Ousman")
                .lastName("SADJO")
                .phone("770000000")
                .build();
        Client client2 = Client.builder()
                .id(2000L)
                .codeClient("CL-2000")
                .firstName("Autre")
                .lastName("CLIENT")
                .build();

        Vehicule vehicule = Vehicule.builder()
                .immatriculation("51c561")
                .marque("BMW")
                .modele("Serie 3")
                .client(client1)
                .build();
        vehicule.setId(5L);

        OrdreReparation or = OrdreReparation.builder()
                .id(10L)
                .numero("DK-OR-2026-0010")
                .vehicule(vehicule)
                .client(client1)
                .build();

        Facture facture = Facture.builder()
                .id(1L)
                .numero("3259")
                .dateCreation(LocalDateTime.of(2026, 11, 3, 10, 0))
                .dateModification(LocalDateTime.of(2026, 11, 3, 10, 5))
                .montantHT(new BigDecimal("250"))
                .montantTVA(new BigDecimal("45"))
                .montantTimbre(BigDecimal.ZERO)
                .montantTTC(new BigDecimal("295"))
                .montantTotal(new BigDecimal("295"))
                .montantPaye(new BigDecimal("200"))
                .resteAPayer(new BigDecimal("95"))
                .statutPaiement(StatutPaiement.PARTIEL)
                .ordreReparation(or)
                .build();

        BonDeLivraison bl = BonDeLivraison.builder()
                .id(2L)
                .numero("2228")
                .dateCreation(LocalDateTime.of(2026, 11, 3, 9, 0))
                .dateModification(LocalDateTime.of(2026, 11, 3, 9, 0))
                .montantHT(new BigDecimal("1000"))
                .montantTVA(new BigDecimal("180"))
                .montantTimbre(BigDecimal.ZERO)
                .montantTTC(new BigDecimal("1180"))
                .montantTotal(new BigDecimal("1180"))
                .vehicule(vehicule)
                .client(client1)
                .build();

        AvoirTTC avoirTtc = AvoirTTC.builder()
                .id(3L)
                .numero("57")
                .dateCreation(LocalDateTime.of(2026, 11, 3, 11, 0))
                .dateModification(LocalDateTime.of(2026, 11, 3, 11, 0))
                .montantHT(new BigDecimal("250"))
                .montantTVA(new BigDecimal("45"))
                .montantTimbre(BigDecimal.ZERO)
                .montantTTC(new BigDecimal("295"))
                .montantTotal(new BigDecimal("295"))
                .statut(StatutFacturation.ACCEPTE)
                .vehicule(vehicule)
                .client(client1)
                .build();

        NoteDePrix note = NoteDePrix.builder()
                .id(4L)
                .numero("7602")
                .dateCreation(LocalDateTime.of(2026, 11, 3, 12, 0))
                .dateModification(LocalDateTime.of(2026, 11, 3, 12, 10))
                .montantHT(new BigDecimal("250"))
                .montantTotal(new BigDecimal("250"))
                .montantPaye(BigDecimal.ZERO)
                .resteAPayer(new BigDecimal("250"))
                .statutPaiement(StatutPaiement.NON_PAYE)
                .vehicule(vehicule)
                .client(client1)
                .build();

        AvoirHT avoirHt = AvoirHT.builder()
                .id(5L)
                .numero("12")
                .dateCreation(LocalDateTime.of(2026, 11, 3, 13, 0))
                .dateModification(LocalDateTime.of(2026, 11, 3, 13, 0))
                .montantHT(new BigDecimal("100"))
                .montantTotal(new BigDecimal("100"))
                .statut(StatutFacturation.ACCEPTE)
                .vehicule(vehicule)
                .client(client1)
                .build();

        Facture autreClientFacture = Facture.builder()
                .id(6L)
                .numero("9999")
                .dateCreation(LocalDateTime.of(2026, 11, 3, 14, 0))
                .montantTTC(new BigDecimal("5000"))
                .montantTotal(new BigDecimal("5000"))
                .client(client2)
                .build();

        LocalDate cible = LocalDate.of(2026, 11, 3);
        LocalDateTime expectedDebut = cible.atStartOfDay();
        LocalDateTime expectedFin = cible.plusDays(1).atStartOfDay();

        when(bonDeLivraisonRepository.findHistoriqueBetween(expectedDebut, expectedFin)).thenReturn(List.of(bl));
        when(factureRepository.findHistoriqueBetween(expectedDebut, expectedFin)).thenReturn(List.of(facture, autreClientFacture));
        when(avoirTTCRepository.findHistoriqueBetween(expectedDebut, expectedFin)).thenReturn(List.of(avoirTtc));
        when(noteDePrixRepository.findHistoriqueBetween(expectedDebut, expectedFin)).thenReturn(List.of(note));
        when(avoirHTRepository.findHistoriqueBetween(expectedDebut, expectedFin)).thenReturn(List.of(avoirHt));

        FacturationHistoriqueResponseDto res = service.getHistorique(
                null, cible, null, null, null, "1667", null, null, null);

        assertEquals(cible, res.getDateDebut());
        assertEquals(cible, res.getDateFin());
        assertEquals(5L, res.getTotalOperations());
        assertEquals(1L, res.getTotalBonsLivraison());
        assertEquals(1L, res.getTotalFactures());
        assertEquals(1L, res.getTotalAvoirsTTC());
        assertEquals(1L, res.getTotalNotesDePrix());
        assertEquals(1L, res.getTotalAvoirsHT());
        assertEquals(new BigDecimal("1180"), res.getMontantTotalBonsLivraisonTTC());
        assertEquals(new BigDecimal("295"), res.getMontantTotalFacturesTTC());
        assertEquals(new BigDecimal("295"), res.getMontantTotalAvoirsTTC());
        assertEquals(new BigDecimal("250"), res.getMontantTotalNotesDePrixHT());
        assertEquals(new BigDecimal("100"), res.getMontantTotalAvoirsHT());

        assertEquals(5, res.getEvenements().size());
        assertEquals(TypeHistoriqueFacturation.AVOIR_HT, res.getEvenements().get(0).getType());
        assertEquals(TypeHistoriqueFacturation.BL, res.getEvenements().get(4).getType());
        assertEquals("51c561", res.getEvenements().get(3).getImmatriculation());
        assertEquals("Ousman SADJO", res.getEvenements().get(3).getClientNom());

        // Filtre par type = NOTE_PRIX avec pagination tout en conservant les totaux du scope
        FacturationHistoriqueResponseDto filtered = service.getHistorique(
                TypeHistoriqueFacturation.NOTE_PRIX, cible, null, null, null, "CL-1667", null, 0, 10);
        assertEquals(5L, filtered.getTotalOperations());
        assertEquals(1L, filtered.getTotalElements());
        assertEquals(1, filtered.getEvenements().size());
        assertEquals("7602", filtered.getEvenements().get(0).getNumero());

        verify(factureRepository, org.mockito.Mockito.times(2)).findHistoriqueBetween(expectedDebut, expectedFin);
    }

    @Test
    void uneSeuleDateFiltreLeJourMemeEtDeuxDatesFiltrentUnePlage() {
        LocalDate jour = LocalDate.of(2026, 11, 3);
        when(bonDeLivraisonRepository.findHistoriqueBetween(any(), any())).thenReturn(List.of());
        when(factureRepository.findHistoriqueBetween(any(), any())).thenReturn(List.of());
        when(avoirTTCRepository.findHistoriqueBetween(any(), any())).thenReturn(List.of());
        when(noteDePrixRepository.findHistoriqueBetween(any(), any())).thenReturn(List.of());
        when(avoirHTRepository.findHistoriqueBetween(any(), any())).thenReturn(List.of());

        FacturationHistoriqueResponseDto debutSeul = service.getHistorique(
                null, null, jour, null, null, null, null, null, null);
        assertEquals(jour, debutSeul.getDateDebut());
        assertEquals(jour, debutSeul.getDateFin());
        verify(factureRepository).findHistoriqueBetween(jour.atStartOfDay(), jour.plusDays(1).atStartOfDay());

        FacturationHistoriqueResponseDto finSeule = service.getHistorique(
                null, null, null, jour, null, null, null, null, null);
        assertEquals(jour, finSeule.getDateDebut());
        assertEquals(jour, finSeule.getDateFin());
        verify(factureRepository, org.mockito.Mockito.times(2))
                .findHistoriqueBetween(jour.atStartOfDay(), jour.plusDays(1).atStartOfDay());

        LocalDate finPlage = LocalDate.of(2026, 11, 10);
        FacturationHistoriqueResponseDto plage = service.getHistorique(
                null, null, jour, finPlage, null, null, null, null, null);
        assertEquals(jour, plage.getDateDebut());
        assertEquals(finPlage, plage.getDateFin());
        verify(factureRepository).findHistoriqueBetween(jour.atStartOfDay(), finPlage.plusDays(1).atStartOfDay());
    }

    @Test
    void rejettePlageDatesInvalide() {
        assertThrows(BadRequestException.class, () ->
                service.getHistorique(null, null, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 1),
                        null, null, null, null, null));
    }
}
