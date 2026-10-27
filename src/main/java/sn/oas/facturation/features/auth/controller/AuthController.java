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
    public ResponseEntity<AuthResponse> signin(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        AuthResponse response = authService.login(request);

        ResponseCookie refreshCookie = buildRefreshCookie(response.refreshToken(), httpRequest);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie("token", httpRequest).toString())
                .header(HttpHeaders.SET_COOKIE, expiredCookie("accessToken", httpRequest).toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le jeton d'accès avec le cookie HttpOnly")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);
        AuthResponse response = authService.refreshToken(refreshToken);

        ResponseCookie refreshCookie = buildRefreshCookie(response.refreshToken(), request);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie("token", request).toString())
                .header(HttpHeaders.SET_COOKIE, expiredCookie("accessToken", request).toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request != null && request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    return cookie.getValue().trim();
                }
            }
        }
        if (request != null) {
            String header = request.getHeader("X-Refresh-Token");
            if (header != null && !header.isBlank()) {
                return header.trim();
            }
        }
        return null;
    }

    private boolean isSecure(HttpServletRequest request) {
        if (cookieSecure) {
            return true;
        }
        if (request == null) {
            return false;
        }
        if (request.isSecure()) {
            return true;
        }
        String proto = request.getHeader("X-Forwarded-Proto");
        if (proto != null && "https".equalsIgnoreCase(proto.trim())) {
            return true;
        }
        String ssl = request.getHeader("X-Forwarded-Ssl");
        if (ssl != null && "on".equalsIgnoreCase(ssl.trim())) {
            return true;
        }
        String origin = request.getHeader("Origin");
        if (origin != null && origin.toLowerCase().startsWith("https://")) {
            return true;
        }
        return false;
    }

    private String resolveSameSite(boolean secure) {
        if (secure) {
            return "None";
        }
        return (cookieSameSite != null && !cookieSameSite.isBlank()) ? cookieSameSite : "Lax";
    }

    private ResponseCookie buildRefreshCookie(String token, HttpServletRequest request) {
        boolean secure = isSecure(request);
        String sameSite = resolveSameSite(secure);
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 jours
                .sameSite(sameSite)
                .build();
    }

    private ResponseCookie expiredCookie(String name, HttpServletRequest request) {
        boolean secure = isSecure(request);
        String sameSite = resolveSameSite(secure);
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(0)
                .sameSite(sameSite)
                .build();
    }

    @PostMapping("/signout")
    @Operation(summary = "Déconnexion et suppression des cookies HttpOnly")
    public ResponseEntity<?> signout(HttpServletRequest request) {
        ResponseCookie accessCookie = expiredCookie("accessToken", request);
        ResponseCookie legacyAccessCookie = expiredCookie("token", request);
        ResponseCookie refreshCookie = expiredCookie("refreshToken", request);
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
