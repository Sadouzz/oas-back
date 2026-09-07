package sn.oas.facturation.features.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sn.oas.facturation.features.auth.dto.request.ChangePasswordRequest;
import sn.oas.facturation.features.auth.dto.request.RegisterRequest;
import sn.oas.facturation.features.auth.service.AuthService;
import sn.oas.facturation.features.user.data.enums.Role;
import sn.oas.facturation.features.user.data.enums.TypeUser;
import sn.oas.facturation.features.user.service.UserService;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.user.dto.request.UserUpdateRequest;
import sn.oas.facturation.features.user.dto.response.UserListResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Gestion des utilisateurs", description = "API pour la gestion des utilisateurs")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/me")
    @Operation(summary = "Récupérer le profil de l'utilisateur connecté")
    public ResponseEntity<UserListResponse> getCurrentUser() {
        return ResponseEntity.ok(UserListResponse.from(userService.getCurrentUser()));
    }

    @PutMapping("/me")
    @Operation(summary = "Mettre à jour le profil de l'utilisateur connecté")
    public ResponseEntity<UserListResponse> updateCurrentUser(@RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(UserListResponse.from(userService.updateCurrentUser(request)));
    }

    @PostMapping("/me/change-password")
    @Operation(summary = "Changer le mot de passe de l'utilisateur connecté")
    public ResponseEntity<?> changeMyPassword(@RequestBody ChangePasswordRequest request) {
        userService.changePasswordForCurrentUser(request.oldPassword(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès !"));
    }

    @GetMapping
    @Operation(summary = "Lister tous les utilisateurs ou rechercher par mot-clé avec pagination")
    public ResponseEntity<Page<UserListResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity
                    .ok(userService.searchUsers(keyword.trim(), page, size).map(UserListResponse::from));
        }
        return ResponseEntity.ok(userService.getAllUsers(page, size).map(UserListResponse::from));
    }

    @GetMapping("/all")
    @Operation(summary = "Lister tous les utilisateurs sans pagination")
    public ResponseEntity<List<UserListResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream().map(UserListResponse::from).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un utilisateur par son ID")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
            return ResponseEntity.ok(UserListResponse.from(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/create")
    @Operation(summary = "Créer un nouvel utilisateur")
    public ResponseEntity<?> createUser(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Compte utilisateur créé avec succès !"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un utilisateur")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        try {
            User updated = userService.updateUser(id, request);
            return ResponseEntity.ok(UserListResponse.from(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archiver un utilisateur")
    public ResponseEntity<?> archiveUser(@PathVariable Long id) {
        try {
            userService.archiveUser(id);
            return ResponseEntity.ok(Map.of("message", "Utilisateur archivé avec succès !"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/unarchive")
    @Operation(summary = "Désarchiver un utilisateur")
    public ResponseEntity<?> unarchiveUser(@PathVariable Long id) {
        try {
            userService.unarchiveUser(id);
            return ResponseEntity.ok(Map.of("message", "Utilisateur désarchivé avec succès !"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Basculer l'état actif/inactif d'un utilisateur")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        try {
            User user = userService.toggleUserStatus(id);
            return ResponseEntity.ok(UserListResponse.from(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé avec succès !"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/roles")
    @Operation(summary = "Lister les rôles disponibles")
    public ResponseEntity<List<String>> getRoles() {
        return ResponseEntity.ok(Arrays.stream(Role.values()).map(Enum::name).toList());
    }

    @GetMapping("/types")
    @Operation(summary = "Lister les types d'utilisateurs disponibles")
    public ResponseEntity<List<String>> getTypes() {
        return ResponseEntity.ok(Arrays.stream(TypeUser.values()).map(Enum::name).toList());
    }
}