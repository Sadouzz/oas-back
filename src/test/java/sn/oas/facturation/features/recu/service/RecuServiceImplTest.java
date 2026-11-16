package sn.oas.facturation.features.recu.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.oas.facturation.features.client.service.PortefeuilleClientService;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.notification.service.AgentNotificationService;
import sn.oas.facturation.features.notification.service.NotificationService;
import sn.oas.facturation.features.recu.data.entity.Recu;
import sn.oas.facturation.features.recu.repository.RecuRepository;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.documentNumber.DocumentType;
import sn.oas.facturation.shared.exception.BadRequestException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecuServiceImplTest {

    @Mock RecuRepository recuRepository;
    @Mock FactureRepository factureRepository;
    @Mock OrdreReparationRepository ordreReparationRepository;
    @Mock DocumentNumberGeneratorService documentNumberGeneratorService;
    @Mock NotificationService notificationService;
    @Mock AgentNotificationService agentNotificationService;
    @Mock PortefeuilleClientService portefeuilleClientService;

    @InjectMocks RecuServiceImpl service;

    @Test
    void paiementIntegralCreeUnRecuEtSoldeLeResteSansAjouterDeTimbre() {
        Facture facture = facturePartiellementPayee();
        when(factureRepository.findByIdForPaiement(18L)).thenReturn(Optional.of(facture));
        when(documentNumberGeneratorService.generateNextNumber(DocumentType.RC)).thenReturn("RC-0018");
        when(recuRepository.save(any(Recu.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Recu recu = service.createPaiementIntegral(18L, "ESPECE");

        assertEquals(new BigDecimal("800.00"), recu.getMontant());
        assertEquals(BigDecimal.ZERO, recu.getMontantTimbre());
        assertEquals("ESPECE", recu.getModePaiement());
        assertEquals(new BigDecimal("1000.00"), facture.getMontantPaye());
        assertEquals(0, BigDecimal.ZERO.compareTo(facture.getResteAPayer()));
        assertEquals(StatutPaiement.PAYE, facture.getStatutPaiement());
        verify(factureRepository).save(facture);
        verify(portefeuilleClientService, never()).debiterPourFacture(any(), any(), any());
    }

    @Test
    void paiementIntegralParSoldeDebiteLePortefeuillePourLeResteExact() {
        Facture facture = facturePartiellementPayee();
        when(factureRepository.findByIdForPaiement(18L)).thenReturn(Optional.of(facture));
        when(documentNumberGeneratorService.generateNextNumber(DocumentType.RC)).thenReturn("RC-0018");
        when(recuRepository.save(any(Recu.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Recu recu = service.createPaiementIntegral(18L, PortefeuilleClientService.MODE_PAIEMENT);

        assertEquals(new BigDecimal("800.00"), recu.getMontant());
        assertEquals(PortefeuilleClientService.MODE_PAIEMENT, recu.getModePaiement());
        verify(portefeuilleClientService).debiterPourFacture(facture, recu, new BigDecimal("800.00"));
    }

    @Test
    void paiementIntegralRefuseUneFactureDejaSoldeeSansCreerDeRecu() {
        Facture facture = facturePartiellementPayee();
        facture.setMontantPaye(new BigDecimal("1000.00"));
        facture.setResteAPayer(BigDecimal.ZERO);
        when(factureRepository.findByIdForPaiement(18L)).thenReturn(Optional.of(facture));

        assertThrows(BadRequestException.class, () -> service.createPaiementIntegral(18L, "ESPECE"));

        verifyNoInteractions(recuRepository);
        verify(factureRepository, never()).save(any());
    }

    private Facture facturePartiellementPayee() {
        return Facture.builder()
                .id(18L)
                .montantTotal(new BigDecimal("1000.00"))
                .montantPaye(new BigDecimal("200.00"))
                .resteAPayer(new BigDecimal("800.00"))
                .statutPaiement(StatutPaiement.PARTIEL)
                .build();
    }
}
