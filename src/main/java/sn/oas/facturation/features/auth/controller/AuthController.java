package sn.oas.facturation.features.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import sn.oas.facturation.features.auth.dto.request.ChangePasswordRequest;
import sn.oas.facturation.features.auth.dto.request.LoginRequest;
import sn.oas.facturation.features.auth.dto.request.RegisterRequest;
import sn.oas.facturation.features.auth.dto.response.AuthResponse;
import sn.oas.facturation.features.user.repository.UserRepository;
import sn.oas.facturation.features.auth.service.AuthService;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.user.data.enums.TypeUser;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "API pour l'authentification et la gestion des mots de passe")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/signin")
    @Operation(summary = "Connexion d'un utilisateur avec cookies HttpOnly (Access token 6h & Refresh token 7j)")
    public ResponseEntity<AuthResponse> signin(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);

        ResponseCookie accessCookie = ResponseCookie.from("token", response.token())
                .httpOnly(true)
                .secure(false) // Mettre true en production HTTPS
                .path("/")
                .maxAge(6 * 60 * 60) // 6 heures
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 jours
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le token d'authentification et les cookies HttpOnly")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);
        AuthResponse response = authService.refreshToken(refreshToken);

        ResponseCookie accessCookie = ResponseCookie.from("token", response.token())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(6 * 60 * 60) // 6 heures
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 jours
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        String customHeader = request.getHeader("X-Refresh-Token");
        if (customHeader != null && !customHeader.isBlank()) {
            return customHeader.trim();
        }
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @PostMapping("/signout")
    @Operation(summary = "Déconnexion et suppression des cookies HttpOnly")
    public ResponseEntity<?> signout() {
        ResponseCookie accessCookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of("message", "Déconnecté avec succès"));
    }

    @PostMapping("/signup")
    @Operation(summary = "Inscription d'un nouvel utilisateur")
    public ResponseEntity<?> signup(@RequestBody RegisterRequest request) {
        // Un compte technicien n'est jamais créé par auto-inscription publique : uniquement
        // via le endpoint staff protégé (TechnicienController / écran gestion/techniciens),
        // qui passe par AuthServiceImpl.register() avec un appelant Agent authentifié.
        // Rejet explicite ici en plus du garde-fou dans register() (défense en profondeur).
        if (request.type() == TypeUser.TECHNICIEN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "La création d'un compte technicien n'est pas autorisée via l'inscription publique."));
        }
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/change-password")
    @Operation(summary = "Changer le mot de passe d'un utilisateur")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Ancien mot de passe incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Mot de passe changé avec succès !");
    }
}
