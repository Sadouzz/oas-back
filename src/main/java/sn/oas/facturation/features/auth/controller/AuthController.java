package sn.oas.facturation.features.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import sn.oas.facturation.features.auth.dto.request.ChangePasswordRequest;
import sn.oas.facturation.features.auth.dto.request.LoginRequest;
import sn.oas.facturation.features.auth.dto.request.RegisterRequest;
import sn.oas.facturation.features.auth.dto.response.AuthResponse;
import sn.oas.facturation.features.user.service.UserService;
import sn.oas.facturation.features.user.dto.request.UserUpdateRequest;
import sn.oas.facturation.features.user.dto.response.UserListResponse;
import sn.oas.facturation.features.auth.service.AuthService;
import sn.oas.facturation.features.user.data.enums.TypeUser;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "API pour l'authentification et la gestion des mots de passe")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    @PostMapping("/signin")
    @Operation(summary = "Connexion avec jeton d'accès et cookie de renouvellement HttpOnly")
    public ResponseEntity<AuthResponse> signin(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.refreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 jours
                .sameSite(cookieSameSite)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredAccessCookie("token").toString())
                .header(HttpHeaders.SET_COOKIE, expiredAccessCookie("accessToken").toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le jeton d'accès avec le cookie HttpOnly")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);
        AuthResponse response = authService.refreshToken(refreshToken);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.refreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 jours
                .sameSite(cookieSameSite)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredAccessCookie("token").toString())
                .header(HttpHeaders.SET_COOKIE, expiredAccessCookie("accessToken").toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    return cookie.getValue().trim();
                }
            }
        }
        return null;
    }

    private ResponseCookie expiredAccessCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();
    }

    @PostMapping("/signout")
    @Operation(summary = "Déconnexion et suppression des cookies HttpOnly")
    public ResponseEntity<?> signout() {
        ResponseCookie accessCookie = expiredAccessCookie("accessToken");
        ResponseCookie legacyAccessCookie = expiredAccessCookie("token");
        ResponseCookie refreshCookie = expiredAccessCookie("refreshToken");
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, legacyAccessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of("message", "Déconnecté avec succès"));
    }

    @PostMapping("/signup")
    @Operation(summary = "Inscription d'un nouvel utilisateur")
    public ResponseEntity<?> signup(@RequestBody @jakarta.validation.Valid RegisterRequest request) {
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
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Changer le mot de passe de l'utilisateur connecté (compatibilité)")
    public ResponseEntity<?> changePassword(@RequestBody @jakarta.validation.Valid ChangePasswordRequest request) {
        userService.changePasswordForCurrentUser(request.oldPassword(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Mot de passe changé avec succès !"));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Récupérer le profil de l'utilisateur connecté")
    public ResponseEntity<UserListResponse> getCurrentUser() {
        return ResponseEntity.ok(UserListResponse.from(userService.getCurrentUser()));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mettre à jour le profil de l'utilisateur connecté")
    public ResponseEntity<UserListResponse> updateCurrentUser(@RequestBody @jakarta.validation.Valid UserUpdateRequest request) {
        return ResponseEntity.ok(UserListResponse.from(userService.updateCurrentUser(request)));
    }

    @PostMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Changer le mot de passe de l'utilisateur connecté")
    public ResponseEntity<?> changeMyPassword(@RequestBody @jakarta.validation.Valid ChangePasswordRequest request) {
        userService.changePasswordForCurrentUser(request.oldPassword(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès !"));
    }
}
