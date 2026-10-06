package sn.oas.facturation.features.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import sn.oas.facturation.features.auth.dto.request.RegisterRequest;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.data.enums.TypeClient;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.connectionHistory.service.ConnectionHistoryService;
import sn.oas.facturation.features.garage.repository.GarageRepository;
import sn.oas.facturation.features.notification.service.EmailService;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.user.data.enums.TypeUser;
import sn.oas.facturation.features.user.repository.UserRepository;
import sn.oas.facturation.features.user.service.UserService;
import sn.oas.facturation.security.JwtUtil;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceEnterpriseRegistrationTest {
    @Mock AuthenticationManager authenticationManager;
    @Mock ConnectionHistoryService connectionHistoryService;
    @Mock HttpServletRequest httpRequest;
    @Mock UserService userService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @Mock UserRepository userRepository;
    @Mock ClientRepository clientRepository;
    @Mock GarageRepository garageRepository;
    @Mock DocumentNumberGeneratorService documentNumberGeneratorService;
    @Mock EmailService emailService;

    @InjectMocks AuthServiceImpl service;

    @Test
    void registerEnterprisePersistsItsLegalAndContactDetails() {
        when(userService.existsByUsername("orientauto1234")).thenReturn(false);
        when(userService.existsByEmail("contact@orientauto.sn")).thenReturn(false);
        when(userService.existsByPhone("+221338200000")).thenReturn(false);
        when(clientRepository.findMaxClientMatricule()).thenReturn(null);
        when(clientRepository.count()).thenReturn(0L);
        when(userService.existsByMatricule("CLT-00001")).thenReturn(false);
        when(passwordEncoder.encode("secret1")).thenReturn("encoded-secret");

        service.register(enterpriseRequest("SN-DKR-2026-A", "contact@orientauto.sn", "+221338200000"));

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userService).saveUser(savedUser.capture());
        Client client = assertInstanceOf(Client.class, savedUser.getValue());
        assertEquals(TypeClient.ENTREPRISE, client.getTypeClient());
        assertEquals("Orient Auto Service", client.getRaisonSociale());
        assertEquals("SN-DKR-2026-A", client.getNumeroEntreprise());
        assertEquals("contact@orientauto.sn", client.getEmailEntreprise());
        assertEquals("Rue 10, Dakar", client.getAdresseEntreprise());
        assertEquals("contact@orientauto.sn", client.getEmail());
        assertEquals("+221338200000", client.getPhone());
    }

    @Test
    void registerEnterpriseRejectsMissingNinea() {
        RegisterRequest request = new RegisterRequest(
                null, "+221338200000", "orientauto1234", "Awa", "Diop", "contact@orientauto.sn",
                "secret1", "secret1", TypeUser.CLIENT, null, null, null, null,
                TypeClient.ENTREPRISE, "Orient Auto Service", " ", "contact@orientauto.sn",
                "+221338200000", "Rue 10, Dakar");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.register(request));

        assertEquals("Le NINEA est obligatoire pour une entreprise.", error.getMessage());
        verifyNoInteractions(userService, clientRepository);
    }

    @Test
    void legacyRegistrationWithoutClientTypeRemainsAParticulier() {
        when(userService.existsByUsername("awa.diop")).thenReturn(false);
        when(userService.existsByEmail("awa@example.sn")).thenReturn(false);
        when(userService.existsByPhone("+221338200000")).thenReturn(false);
        when(clientRepository.findMaxClientMatricule()).thenReturn(null);
        when(clientRepository.count()).thenReturn(0L);
        when(userService.existsByMatricule("CLT-00001")).thenReturn(false);
        when(passwordEncoder.encode("secret1")).thenReturn("encoded-secret");

        service.register(new RegisterRequest(
                null, "+221338200000", "awa.diop", "Awa", "Diop", "awa@example.sn",
                "secret1", "secret1", TypeUser.CLIENT, null, null, null, null,
                null, null, null, null, null, null));

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userService).saveUser(savedUser.capture());
        Client client = assertInstanceOf(Client.class, savedUser.getValue());
        assertEquals(TypeClient.PARTICULIER, client.getTypeClient());
        assertNull(client.getRaisonSociale());
        assertNull(client.getNumeroEntreprise());
    }

    private RegisterRequest enterpriseRequest(String ninea, String email, String phone) {
        return new RegisterRequest(
                null, phone, "orientauto1234", "Awa", "Diop", email, "secret1", "secret1",
                TypeUser.CLIENT, null, null, null, null, TypeClient.ENTREPRISE,
                "Orient Auto Service", ninea, email, phone, "Rue 10, Dakar");
    }
}
