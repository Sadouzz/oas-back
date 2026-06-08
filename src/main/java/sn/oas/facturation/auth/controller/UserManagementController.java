package sn.oas.facturation.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.auth.dto.RegisterRequest;
import sn.oas.facturation.auth.dto.UserUpdateRequest;
import sn.oas.facturation.auth.data.entity.User;
import sn.oas.facturation.auth.service.AuthService;
import sn.oas.facturation.auth.service.UserService;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Gestion des utilisateurs", description = "API d'administration pour la gestion des utilisateurs")
public class UserManagementController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping
    @Operation(summary = "Lister tous les utilisateurs ou rechercher par mot-clé")
    public ResponseEntity<?> listUsers(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(userService.searchUsers(keyword));
        }
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un utilisateur par son ID")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create")
    @Operation(summary = "Créer un nouvel utilisateur")
    public ResponseEntity<?> createUser(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.ok("Compte utilisateur créé avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un utilisateur")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        try {
            return ResponseEntity.ok(userService.updateUser(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archiver un utilisateur")
    public ResponseEntity<?> archiveUser(@PathVariable Long id) {
        try {
            userService.archiveUser(id);
            return ResponseEntity.ok("Utilisateur archivé avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/unarchive")
    @Operation(summary = "Désarchiver un utilisateur")
    public ResponseEntity<?> unarchiveUser(@PathVariable Long id) {
        try {
            userService.unarchiveUser(id);
            return ResponseEntity.ok("Utilisateur désarchivé avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("Utilisateur supprimé avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}