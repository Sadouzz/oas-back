package sn.oas.facturation.features.push.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.push.data.dto.PushSubscriptionRequest;
import sn.oas.facturation.features.push.service.PushService;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.user.repository.UserRepository;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushController {
    private final PushService pushService;
    private final UserRepository userRepository;

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName()).orElseGet(() -> userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Utilisateur introuvable.")));
    }

    @GetMapping("/public-key")
    public ResponseEntity<String> publicKey() {
        if (pushService.publicKey() == null || pushService.publicKey().isBlank()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(pushService.publicKey());
    }

    @PutMapping("/subscription")
    public ResponseEntity<Void> subscribe(Authentication authentication, @Valid @RequestBody PushSubscriptionRequest request) {
        pushService.upsert(currentUser(authentication), request.endpoint(), request.p256dh(), request.auth());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/subscription")
    public ResponseEntity<Void> unsubscribe(Authentication authentication, @RequestParam String endpoint) {
        pushService.remove(currentUser(authentication), endpoint);
        return ResponseEntity.noContent().build();
    }
}
