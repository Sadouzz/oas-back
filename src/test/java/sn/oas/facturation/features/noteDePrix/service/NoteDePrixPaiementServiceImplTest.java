package sn.oas.facturation.features.noteDePrix.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.client.service.PortefeuilleClientService;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.garage.repository.GarageRepository;
import sn.oas.facturation.features.main_doeuvre.repository.MainDoeuvreRepository;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.features.noteDePrix.data.entity.PaiementNoteDePrix;
import sn.oas.facturation.features.noteDePrix.dto.PaiementNoteDePrixRequest;
import sn.oas.facturation.features.noteDePrix.repository.NoteDePrixRepository;
import sn.oas.facturation.features.noteDePrix.repository.PaiementNoteDePrixRepository;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.piecedetache.repository.PDPRepository;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.features.piecedetache.repository.PieceMouvementRepository;
import sn.oas.facturation.features.user.repository.UserRepository;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.exception.BadRequestException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteDePrixPaiementServiceImplTest {

    @Mock NoteDePrixRepository noteDePrixRepository;
    @Mock OrdreReparationRepository ordreReparationRepository;
    @Mock PDPRepository pdpRepository;
    @Mock PieceDetacheRepository pieceDetacheRepository;
    @Mock MainDoeuvreRepository mainDoeuvreRepository;
    @Mock ClientRepository clientRepository;
    @Mock VehiculeRepository vehiculeRepository;
    @Mock UserRepository userRepository;
    @Mock DocumentNumberGeneratorService documentNumberGeneratorService;
    @Mock PieceMouvementRepository pieceMouvementRepository;
    @Mock GarageRepository garageRepository;
    @Mock PaiementNoteDePrixRepository paiementNoteDePrixRepository;
    @Mock PortefeuilleClientService portefeuilleClientService;

    @InjectMocks NoteDePrixServiceImpl service;

    @Test
    void enregistreUneTrancheEtRecalculeLeSoldeRestant() {
        NoteDePrix note = note(200_000, 0);
        when(noteDePrixRepository.findByIdForPaiement(7L)).thenReturn(Optional.of(note));
        when(paiementNoteDePrixRepository.save(any(PaiementNoteDePrix.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var paiement = service.enregistrerPaiement(7L,
                new PaiementNoteDePrixRequest(new BigDecimal("75000.00"), "ESPECE"));

        assertEquals(new BigDecimal("75000.00"), paiement.montant());
        assertEquals("ESPECE_TTC", paiement.modePaiement());
        assertEquals(new BigDecimal("75000.00"), note.getMontantPaye());
        assertEquals(new BigDecimal("125000.00"), note.getResteAPayer());
        assertEquals(StatutPaiement.PARTIEL, note.getStatutPaiement());
        verify(noteDePrixRepository).save(note);
        verify(portefeuilleClientService, never()).debiterPourNoteDePrix(any(), any(), any());
    }

    @Test
    void soldePrepayeDebiteExactementLaTrancheEtSoldeLaNote() {
        NoteDePrix note = note(100_000, 0);
        when(noteDePrixRepository.findByIdForPaiement(7L)).thenReturn(Optional.of(note));
        when(paiementNoteDePrixRepository.save(any(PaiementNoteDePrix.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var paiement = service.enregistrerPaiement(7L,
                new PaiementNoteDePrixRequest(new BigDecimal("100000.00"), PortefeuilleClientService.MODE_PAIEMENT));

        assertEquals(new BigDecimal("100000.00"), note.getMontantPaye());
        assertEquals(BigDecimal.ZERO.setScale(2), note.getResteAPayer());
        assertEquals(StatutPaiement.PAYE, note.getStatutPaiement());
        assertEquals(PortefeuilleClientService.MODE_PAIEMENT, paiement.modePaiement());
        verify(portefeuilleClientService).debiterPourNoteDePrix(eq(note), any(PaiementNoteDePrix.class), eq(new BigDecimal("100000.00")));
    }

    @Test
    void refuseUnPaiementSuperieurAuSoldeSansCreerDeTranche() {
        NoteDePrix note = note(200_000, 150_000);
        when(noteDePrixRepository.findByIdForPaiement(7L)).thenReturn(Optional.of(note));

        assertThrows(BadRequestException.class, () -> service.enregistrerPaiement(7L,
                new PaiementNoteDePrixRequest(new BigDecimal("50001.00"), "ESPECE")));

        verifyNoInteractions(paiementNoteDePrixRepository, portefeuilleClientService);
        verify(noteDePrixRepository, never()).save(any());
    }

    @Test
    void refuseMontantAvecPlusDeDeuxDecimales() {
        assertThrows(BadRequestException.class, () -> service.enregistrerPaiement(7L,
                new PaiementNoteDePrixRequest(new BigDecimal("1.001"), "ESPECE")));

        verifyNoInteractions(noteDePrixRepository, paiementNoteDePrixRepository, portefeuilleClientService);
    }

    private NoteDePrix note(long total, long paid) {
        BigDecimal totalAmount = BigDecimal.valueOf(total).setScale(2);
        BigDecimal paidAmount = BigDecimal.valueOf(paid).setScale(2);
        return NoteDePrix.builder()
                .id(7L)
                .garage(new Garage())
                .montantTotal(totalAmount)
                .montantPaye(paidAmount)
                .resteAPayer(totalAmount.subtract(paidAmount))
                .statutPaiement(paid == 0 ? StatutPaiement.NON_PAYE : StatutPaiement.PARTIEL)
                .build();
    }
}
