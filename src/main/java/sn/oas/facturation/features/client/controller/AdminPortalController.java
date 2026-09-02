package sn.oas.facturation.features.client.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.auth.data.entity.Agent;
import sn.oas.facturation.features.auth.service.AuthService;
import sn.oas.facturation.features.messagerie.dto.ClientConversationResponse;
import sn.oas.facturation.features.messagerie.dto.MessageRequest;
import sn.oas.facturation.features.messagerie.dto.MessageResponse;
import sn.oas.facturation.features.messagerie.service.MessageService;
import sn.oas.facturation.features.recu.dto.RecuRequest;
import sn.oas.facturation.features.recu.dto.RecuResponse;
import sn.oas.facturation.features.recu.service.RecuService;
import sn.oas.facturation.features.rendezvous.data.enums.RendezVousStatus;
import sn.oas.facturation.features.rendezvous.dto.RendezVousResponse;
import sn.oas.facturation.features.rendezvous.service.RendezVousService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/portal")
@RequiredArgsConstructor
@Tag(name = "Gestion Portail Client (Backoffice)", description = "APIs d'administration pour la gestion du Portail Client")
public class AdminPortalController {

    private final RendezVousService rendezvousService;
    private final RecuService recuService;
    private final MessageService messageService;
    private final AuthService authService;

    // --- Rendez-vous ---
    @GetMapping("/rendezvous")
    @Operation(summary = "Lister tous les rendez-vous clients")
    public ResponseEntity<List<RendezVousResponse>> getAllRendezVous() {
        return ResponseEntity.ok(rendezvousService.getAllRendezVous());
    }

    @PutMapping("/rendezvous/{id}/statut")
    @Operation(summary = "Mettre à jour le statut d'un rendez-vous client")
    public ResponseEntity<?> updateRendezVousStatus(
            @PathVariable Long id,
            @RequestParam RendezVousStatus statut,
            @RequestParam(required = false) String commentaire) {
        try {
            return ResponseEntity.ok(rendezvousService.updateRendezVousStatus(id, statut, commentaire));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/rendezvous/{id}/valider")
    @Operation(summary = "Valider un rendez-vous et créer une fiche atelier")
    public ResponseEntity<?> validerRendezVous(
            @PathVariable Long id,
            @RequestBody List<Long> mecanicienIds) {
        try {
            RendezVousResponse response = rendezvousService.validerRendezVous(id, mecanicienIds);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/rendezvous/{id}/date")
    @Operation(summary = "Modifier la date d'un rendez-vous")
    public ResponseEntity<?> updateDate(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        try {
            String dateStr = body.get("nouvelleDate");
            // Handle both "2026-08-15T10:30" and "2026-08-15T10:30:00" formats
            java.time.LocalDateTime nouvelleDate;
            if (dateStr.length() == 16) {
                nouvelleDate = java.time.LocalDateTime.parse(dateStr + ":00");
            } else {
                nouvelleDate = java.time.LocalDateTime.parse(dateStr);
            }
            RendezVousResponse response = rendezvousService.updateDate(id, nouvelleDate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- Paiement & Reçu ---
    @PostMapping("/factures/{id}/payer")
    @Operation(summary = "Enregistrer un paiement (total ou partiel) pour une facture")
    public ResponseEntity<?> registerPayment(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal montant,
            @RequestParam String methodePaiement) {
        try {
            RecuRequest request = RecuRequest.builder()
                .factureId(id)
                .montant(montant)
                .modePaiement(methodePaiement)
                .build();
            RecuResponse response = recuService.create(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- Messagerie Dashboard ---
    @GetMapping("/recus")
    @Operation(summary = "Lister tous les reçus de paiement")
    public ResponseEntity<?> getAllRecus() {
        try {
            return ResponseEntity.ok(recuService.getAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/messages/clients")
    @Operation(summary = "Lister les conversations actives avec les clients")
    public ResponseEntity<List<ClientConversationResponse>> getActiveConversations() {
        return ResponseEntity.ok(messageService.getActiveConversations());
    }

    @GetMapping("/messages/clients/{clientId}")
    @Operation(summary = "Récupérer la discussion d'un client spécifique")
    public ResponseEntity<List<MessageResponse>> getClientMessages(@PathVariable Long clientId) {
        Agent agent = authService.getAgentConnecte();
        return ResponseEntity.ok(messageService.getConversationMessages(clientId, agent));
    }

    @PostMapping("/messages/clients/{clientId}")
    @Operation(summary = "Envoyer un message de réponse à un client")
    public ResponseEntity<?> sendReply(
            @PathVariable Long clientId,
            @RequestBody MessageRequest request) {
        try {
            Agent agent = authService.getAgentConnecte();
            MessageResponse response = messageService.agentSendMessage(agent, clientId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
