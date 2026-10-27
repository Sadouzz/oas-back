package sn.oas.facturation.features.auth.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import sn.oas.facturation.features.auth.dto.request.LoginRequest;
import sn.oas.facturation.features.auth.dto.response.AuthResponse;
import sn.oas.facturation.features.auth.service.AuthService;
import sn.oas.facturation.features.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerCookieTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authController, "cookieSecure", false);
        ReflectionTestUtils.setField(authController, "cookieSameSite", "Lax");
    }

    @Test
    void signinFromVercelHttpsGeneratesSameSiteNoneAndSecure() {
        AuthResponse mockAuthResponse = AuthResponse.of("access-token-123", "refresh-token-456", "agent1", "AGENT", 1L, "Garage 1");
        when(authService.login(any(LoginRequest.class))).thenReturn(mockAuthResponse);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "https://oas-front.vercel.app");
        request.addHeader("X-Forwarded-Proto", "https");

        LoginRequest loginRequest = new LoginRequest("agent1", "password123");
        ResponseEntity<AuthResponse> response = authController.signin(loginRequest, request);

        List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookies);

        String refreshCookie = setCookies.stream()
                .filter(c -> c.startsWith("refreshToken="))
                .findFirst()
                .orElse(null);

        assertNotNull(refreshCookie);
        assertTrue(refreshCookie.contains("SameSite=None"), "Should contain SameSite=None for HTTPS origin");
        assertTrue(refreshCookie.contains("Secure"), "Should contain Secure for HTTPS origin");
        assertTrue(refreshCookie.contains("HttpOnly"), "Should contain HttpOnly");
    }

    @Test
    void signinLocalHttpGeneratesSameSiteLaxWithoutSecure() {
        AuthResponse mockAuthResponse = AuthResponse.of("access-token-123", "refresh-token-456", "agent1", "AGENT", 1L, "Garage 1");
        when(authService.login(any(LoginRequest.class))).thenReturn(mockAuthResponse);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "http://localhost:4200");
        request.setSecure(false);

        LoginRequest loginRequest = new LoginRequest("agent1", "password123");
        ResponseEntity<AuthResponse> response = authController.signin(loginRequest, request);

        List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookies);

        String refreshCookie = setCookies.stream()
                .filter(c -> c.startsWith("refreshToken="))
                .findFirst()
                .orElse(null);

        assertNotNull(refreshCookie);
        assertTrue(refreshCookie.contains("SameSite=Lax"), "Should contain SameSite=Lax for HTTP localhost");
        assertFalse(refreshCookie.contains("Secure"), "Should not contain Secure for plain HTTP");
    }

    @Test
    void signoutCrossSiteMatchesSameSiteNoneAndSecure() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "https://oas-front.vercel.app");

        ResponseEntity<?> response = authController.signout(request);

        List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookies);

        String refreshCookie = setCookies.stream()
                .filter(c -> c.startsWith("refreshToken="))
                .findFirst()
                .orElse(null);

        assertNotNull(refreshCookie);
        assertTrue(refreshCookie.contains("Max-Age=0"));
        assertTrue(refreshCookie.contains("SameSite=None"));
        assertTrue(refreshCookie.contains("Secure"));
    }
}

