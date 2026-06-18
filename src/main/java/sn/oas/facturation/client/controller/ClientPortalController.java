package sn.oas.facturation.client.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.client.service.ClientService;
import sn.oas.facturation.facture.data.entity.Facture;
import sn.oas.facturation.facture.repository.FactureRepository;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.messagerie.dto.MessageRequest;
import sn.oas.facturation.messagerie.dto.MessageResponse;
import sn.oas.facturation.messagerie.service.MessageService;
import sn.oas.facturation.notification.dto.NotificationResponse;
import sn.oas.facturation.notification.service.NotificationService;
import sn.oas.facturation.proforma.dto.ProformaResponse;
import sn.oas.facturation.proforma.service.ProformaService;
import sn.oas.facturation.recu.dto.RecuResponse;
import sn.oas.facturation.recu.service.RecuService;
import sn.oas.facturation.rendezvous.dto.RendezVousRequest;
import sn.oas.facturation.rendezvous.dto.RendezVousResponse;
import sn.oas.facturation.rendezvous.service.RendezVousService;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import sn.oas.facturation.vehicule.dto.VehiculeRequest;
import sn.oas.facturation.vehicule.service.VehiculeService;

import java.util.List;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@Tag(name = "Portail Client", description = "API destinées à l'application mobile client (Flutter)")
public class ClientPortalController {

    private final ClientService clientService;
    private final VehiculeService vehiculeService;
    private final RendezVousService rendezvousService;
    private final FicheAtelierRepository ficheAtelierRepository;
    private final FactureRepository factureRepository;
    private final RecuService recuService;
    private final NotificationService notificationService;
    private final MessageService messageService;
    private final ProformaService proformaService;

    // --- Profil ---
    @GetMapping("/me")
    @Operation(summary = "Récupérer le profil du client connecté")
    public ResponseEntity<Client> getProfile() {
        return ResponseEntity.ok(clientService.getClientConnecte());
    }

    // --- Véhicules ---
    @GetMapping("/vehicules")
    @Operation(summary = "Lister les véhicules du client connecté")
    public ResponseEntity<List<Vehicule>> getVehicules() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(vehiculeService.getVehiculesByClient(client.getId()));
    }

    @PostMapping("/vehicules")
    @Operation(summary = "Enregistrer un véhicule pour le client connecté")
    public ResponseEntity<?> addVehicule(@RequestBody VehiculeRequest request) {
        try {
            Client client = clientService.getClientConnecte();
            VehiculeRequest securedRequest = new VehiculeRequest(
                    request.immatriculation(),
                    request.annee(),
                    request.modele(),
                    request.marque(),
                    request.kilometrage(),
                    request.numeroChassis(),
                    client.getId()
            );
            return ResponseEntity.ok(vehiculeService.createVehicule(securedRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- Rendez-vous ---
    @GetMapping("/rendezvous")
    @Operation(summary = "Lister les rendez-vous du client connecté")
    public ResponseEntity<List<RendezVousResponse>> getRendezVous() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(rendezvousService.getClientRendezVous(client));
    }

    @PostMapping("/rendezvous")
    @Operation(summary = "Prendre un rendez-vous pour le client connecté")
    public ResponseEntity<?> bookRendezVous(@RequestBody RendezVousRequest request) {
        try {
            Client client = clientService.getClientConnecte();
            return ResponseEntity.ok(rendezvousService.bookRendezVous(client, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/rendezvous/{id}/annuler")
    @Operation(summary = "Annuler un rendez-vous")
    public ResponseEntity<?> cancelRendezVous(@PathVariable Long id) {
        try {
            Client client = clientService.getClientConnecte();
            return ResponseEntity.ok(rendezvousService.cancelRendezVous(client, id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- Suivi des réparations/interventions ---
    @GetMapping("/interventions")
    @Operation(summary = "Lister l'historique des interventions/réparations")
    public ResponseEntity<List<FicheAtelier>> getInterventions() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(ficheAtelierRepository.findByVehiculeClientIdOrderByDateCreationDesc(client.getId()));
    }

    @GetMapping("/interventions/{id}")
    @Operation(summary = "Détail d'une intervention/réparation")
    public ResponseEntity<?> getInterventionById(@PathVariable Long id) {
        Client client = clientService.getClientConnecte();
        FicheAtelier fiche = ficheAtelierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Intervention non trouvée"));
        if (!fiche.getVehicule().getClient().getId().equals(client.getId())) {
            return ResponseEntity.badRequest().body("Accès non autorisé à cette intervention");
        }
        return ResponseEntity.ok(fiche);
    }

    // --- Facturation ---
    @GetMapping("/factures")
    @Operation(summary = "Lister l'historique de facturation du client connecté")
    public ResponseEntity<List<Facture>> getFactures() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(factureRepository.findByClientIdOrderByDateCreationDesc(client.getId()));
    }

    @GetMapping("/factures/{id}")
    @Operation(summary = "Récupérer le détail d'une facture")
    public ResponseEntity<?> getFactureById(@PathVariable Long id) {
        Client client = clientService.getClientConnecte();
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée"));
        if (!facture.getClient().getId().equals(client.getId())) {
            return ResponseEntity.badRequest().body("Accès non autorisé à cette facture");
        }
        return ResponseEntity.ok(facture);
    }

    // --- Reçus ---
    @GetMapping("/recus")
    @Operation(summary = "Lister l'historique des reçus de paiement du client connecté")
    public ResponseEntity<List<RecuResponse>> getRecus() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(recuService.getClientRecus(client));
    }

    // --- Notifications ---
    @GetMapping("/notifications")
    @Operation(summary = "Lister les notifications du client connecté")
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(notificationService.getClientNotifications(client));
    }

    @PutMapping("/notifications/{id}/lu")
    @Operation(summary = "Marquer une notification comme lue")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long id) {
        try {
            Client client = clientService.getClientConnecte();
            notificationService.markAsRead(client, id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/notifications/lu-tout")
    @Operation(summary = "Marquer toutes les notifications comme lues")
    public ResponseEntity<?> markAllNotificationsAsRead() {
        try {
            Client client = clientService.getClientConnecte();
            notificationService.markAllAsRead(client);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- Messagerie Client ---
    @GetMapping("/messages")
    @Operation(summary = "Récupérer la discussion du client connecté")
    public ResponseEntity<List<MessageResponse>> getMessages() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(messageService.getConversationMessages(client.getId(), client));
    }

    @PostMapping("/messages")
    @Operation(summary = "Envoyer un message de la part du client connecté")
    public ResponseEntity<?> sendMessage(@RequestBody MessageRequest request) {
        try {
            Client client = clientService.getClientConnecte();
            return ResponseEntity.ok(messageService.clientSendMessage(client, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- Proformas Client ---
    @GetMapping("/proformas")
    @Operation(summary = "Lister les proformas du client connecté")
    public ResponseEntity<List<ProformaResponse>> getProformas() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(proformaService.getClientProformas(client));
    }

    @PutMapping("/proformas/{id}/valider")
    @Operation(summary = "Valider un proforma par le client connecté")
    public ResponseEntity<?> validerProforma(@PathVariable Long id) {
        try {
            Client client = clientService.getClientConnecte();
            return ResponseEntity.ok(proformaService.clientValider(id, client));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/proformas/{id}/refuser")
    @Operation(summary = "Refuser un proforma par le client connecté")
    public ResponseEntity<?> refuserProforma(@PathVariable Long id) {
        try {
            Client client = clientService.getClientConnecte();
            return ResponseEntity.ok(proformaService.clientRefuser(id, client));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
