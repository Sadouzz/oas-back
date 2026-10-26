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
        CompteClientRequest request = new CompteClientRequest("Garage Awa", "sn123abc", 30,
                new BigDecimal("500000"), "SN-DKR-2020-B-1234", "SN08 0000 0000 0000", true);

        service.configurer(22L, request);

        assertEquals(TypeClient.ENTREPRISE, client.getTypeClient());
        assertEquals("SN123ABC", client.getNumeroEntreprise());
        assertEquals("https://cloudinary.test/ancien-justificatif", client.getNinea());
        assertEquals(30, client.getEcheance());
        assertEquals(new BigDecimal("500000"), client.getMontantPlafondEcheance());
        verify(compteRepository).save(any(CompteClient.class));
    }

    @Test void refuseUnNineaDejaAttribue() {
        Client client = Client.builder().id(22L).build();
        when(clientRepository.findById(22L)).thenReturn(Optional.of(client));
        when(clientRepository.existsByNumeroEntrepriseIgnoreCaseAndIdNot("SN123ABC", 22L)).thenReturn(true);
        CompteClientRequest request = new CompteClientRequest("Garage Awa", "SN123ABC", 30,
                new BigDecimal("500000"), "SN-DKR-2020-B-1234", "SN08 0000 0000 0000", true);
        assertThrows(IllegalArgumentException.class, () -> service.configurer(22L, request));
        verify(compteRepository, never()).save(any());
    }

    @Test void refuseUneEcheanceInvalide() {
        CompteClientRequest request = new CompteClientRequest("Garage Awa", "SN123ABC", 0,
                new BigDecimal("500000"), "SN-DKR-2020-B-1234", "SN08 0000 0000 0000", true);
        assertThrows(IllegalArgumentException.class, () -> service.configurer(22L, request));
        verifyNoInteractions(clientRepository, compteRepository, factureRepository);
    }

    @Test void refuseUnPlafondDePeriodeAbsent() {
        CompteClientRequest request = new CompteClientRequest("Garage Awa", "SN123ABC", 30,
                null, "SN-DKR-2020-B-1234", "SN08 0000 0000 0000", true);
        assertThrows(IllegalArgumentException.class, () -> service.configurer(22L, request));
        verifyNoInteractions(clientRepository, compteRepository, factureRepository);
    }
}
