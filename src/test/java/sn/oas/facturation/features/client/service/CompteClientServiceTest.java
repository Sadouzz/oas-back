package sn.oas.facturation.features.client.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.data.entity.CompteClient;
import sn.oas.facturation.features.client.data.enums.TypeClient;
import sn.oas.facturation.features.client.dto.CompteClientRequest;
import sn.oas.facturation.features.client.repository.*;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompteClientServiceTest {
    @Mock ClientRepository clientRepository;
    @Mock CompteClientRepository compteRepository;
    @Mock MouvementCreditClientRepository mouvementRepository;
    @Mock FactureRepository factureRepository;
    @Mock UserRepository userRepository;
    @InjectMocks CompteClientService service;

    @Test void ouvrirCompteConvertitLeClientEtConserveLeLienDuNineaDocument() {
        Client client = Client.builder().id(22L).firstName("Awa").lastName("Diop").email("awa@example.test")
                .adresse("Dakar").ninea("https://cloudinary.test/ancien-justificatif").typeClient(TypeClient.PARTICULIER).build();
        when(clientRepository.findById(22L)).thenReturn(Optional.of(client));
        when(clientRepository.existsByNumeroEntrepriseIgnoreCaseAndIdNot("SN123ABC", 22L)).thenReturn(false);
        when(compteRepository.findByClientId(22L)).thenReturn(Optional.empty());
        when(factureRepository.sumResteAPayerByClientId(22L)).thenReturn(BigDecimal.ZERO);
        when(factureRepository.sumMontantFacturesDepuis(eq(22L), any())).thenReturn(BigDecimal.ZERO);
        when(factureRepository.existsFactureImpayeeEnRetard(eq(22L), any())).thenReturn(false);
        when(factureRepository.existsFactureLegacyImpayeeEnRetard(eq(22L), any())).thenReturn(false);
        CompteClientRequest request = new CompteClientRequest("Garage Awa", "sn123abc", 10,
                new BigDecimal("150000"), 30, new BigDecimal("500000"), null, null, true);

        service.configurer(22L, request);

        assertEquals(TypeClient.ENTREPRISE, client.getTypeClient());
        assertEquals("SN123ABC", client.getNumeroEntreprise());
        assertEquals("https://cloudinary.test/ancien-justificatif", client.getNinea());
        assertEquals(150000, client.getMontantPlafond());
        assertEquals(new BigDecimal("500000"), client.getMontantPlafondEcheance());
        verify(compteRepository).save(any(CompteClient.class));
    }

    @Test void refuseUnNineaDejaAttribue() {
        Client client = Client.builder().id(22L).build();
        when(clientRepository.findById(22L)).thenReturn(Optional.of(client));
        when(clientRepository.existsByNumeroEntrepriseIgnoreCaseAndIdNot("SN123ABC", 22L)).thenReturn(true);
        CompteClientRequest request = new CompteClientRequest("Garage Awa", "SN123ABC", 10,
                null, null, null, null, null, true);
        assertThrows(IllegalArgumentException.class, () -> service.configurer(22L, request));
        verify(compteRepository, never()).save(any());
    }

    @Test void unPutDuCompteEffaceLesConditionsEnvoyeesCommeNull() {
        Client client = Client.builder().id(22L).typeClient(TypeClient.ENTREPRISE)
                .montantPlafond(150000).echeance(30).montantPlafondEcheance(new BigDecimal("500000")).build();
        when(clientRepository.findById(22L)).thenReturn(Optional.of(client));
        when(clientRepository.existsByNumeroEntrepriseIgnoreCaseAndIdNot("SN123ABC", 22L)).thenReturn(false);
        when(compteRepository.findByClientId(22L)).thenReturn(Optional.of(CompteClient.builder().client(client).actif(true).build()));
        when(factureRepository.sumResteAPayerByClientId(22L)).thenReturn(BigDecimal.ZERO);
        when(factureRepository.sumMontantFacturesDepuis(eq(22L), any())).thenReturn(BigDecimal.ZERO);
        when(factureRepository.existsFactureImpayeeEnRetard(eq(22L), any())).thenReturn(false);

        service.configurer(22L, new CompteClientRequest("Garage Awa", "SN123ABC", 0,
                null, null, null, null, null, true));

        assertNull(client.getMontantPlafond());
        assertNull(client.getEcheance());
        assertNull(client.getMontantPlafondEcheance());
    }

    @Test void refuseUnPlafondDePeriodeSansDelaiDePaiement() {
        CompteClientRequest request = new CompteClientRequest("Garage Awa", "SN123ABC", 0,
                null, null, new BigDecimal("500000"), null, null, true);
        assertThrows(IllegalArgumentException.class, () -> service.configurer(22L, request));
        verifyNoInteractions(clientRepository, compteRepository, factureRepository);
    }
}
