package sn.oas.facturation.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.auth.repository.UserRepository;
import sn.oas.facturation.notification.dto.AgentNotificationResponse;
import sn.oas.facturation.notification.service.AgentNotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agent-notifications")
@RequiredArgsConstructor
public class AgentNotificationController {

    private final AgentNotificationService agentNotificationService;
    private final UserRepository userRepository;

    private Agent getAuthenticatedAgent(Authentication authentication) {
        if (authentication == null) return null;
        return (Agent) userRepository.findByUsername(authentication.getName())
                .or(() -> userRepository.findByEmail(authentication.getName()))
                .orElse(null);
    }

    @GetMapping
    public ResponseEntity<List<AgentNotificationResponse>> getNotifications(Authentication authentication) {
        Agent agent = getAuthenticatedAgent(authentication);
        if (agent == null) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(agentNotificationService.getAgentNotifications(agent));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Authentication authentication) {
        Agent agent = getAuthenticatedAgent(authentication);
        if (agent == null) return ResponseEntity.status(401).build();

        agentNotificationService.markAsRead(agent, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        Agent agent = getAuthenticatedAgent(authentication);
        if (agent == null) return ResponseEntity.status(401).build();

        agentNotificationService.markAllAsRead(agent);
        return ResponseEntity.ok().build();
    }
}
