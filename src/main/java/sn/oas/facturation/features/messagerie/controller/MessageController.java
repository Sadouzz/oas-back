package sn.oas.facturation.features.messagerie.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sn.oas.facturation.features.auth.service.AuthService;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.service.ClientService;
import sn.oas.facturation.features.messagerie.dto.ClientConversationResponse;
import sn.oas.facturation.features.messagerie.dto.MessageRequest;
import sn.oas.facturation.features.messagerie.dto.MessageResponse;
import sn.oas.facturation.features.messagerie.service.MessageService;
import sn.oas.facturation.features.user.data.entity.Agent;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messagerie", description = "API pour la gestion de la messagerie instantanée (Clients et Backoffice)")
public class MessageController {

    private final MessageService messageService;
    private final AuthService authService;
    private final ClientService clientService;

    // --- Backoffice / Agent ---
    @GetMapping({"/conversations", "/clients"})
    @Operation(summary = "Lister les conversations actives avec les clients (Agent)")
    public ResponseEntity<List<ClientConversationResponse>> getActiveConversations() {
        return ResponseEntity.ok(messageService.getActiveConversations());
    }

    @GetMapping("/clients/{clientId}")
    @Operation(summary = "Récupérer la discussion d'un client spécifique (Agent)")
    public ResponseEntity<List<MessageResponse>> getClientMessages(@PathVariable Long clientId) {
        Agent agent = authService.getAgentConnecte();
        return ResponseEntity.ok(messageService.getConversationMessages(clientId, agent).stream().map(MessageResponse::from).toList());
    }

    @PostMapping("/clients/{clientId}")
    @Operation(summary = "Envoyer un message de réponse à un client (Agent)")
    public ResponseEntity<MessageResponse> sendReply(
            @PathVariable Long clientId,
            @RequestBody MessageRequest request) {
        Agent agent = authService.getAgentConnecte();
        return ResponseEntity.ok(MessageResponse.from(messageService.agentSendMessage(agent, clientId, request)));
    }

    // --- Client ---
    @GetMapping({"", "/me"})
    @Operation(summary = "Récupérer la discussion du client connecté")
    public ResponseEntity<List<MessageResponse>> getMyMessages() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(messageService.getConversationMessages(client.getId(), client).stream().map(MessageResponse::from).toList());
    }

    @PostMapping({"", "/me"})
    @Operation(summary = "Envoyer un message de la part du client connecté")
    public ResponseEntity<MessageResponse> sendMyMessage(@RequestBody MessageRequest request) {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(MessageResponse.from(messageService.clientSendMessage(client, request)));
    }
}
