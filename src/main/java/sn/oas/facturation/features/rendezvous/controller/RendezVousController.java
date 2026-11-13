package sn.oas.facturation.features.rendezvous.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.service.ClientService;
import sn.oas.facturation.features.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.features.rendezvous.data.enums.RendezVousStatus;
import sn.oas.facturation.features.rendezvous.dto.RendezVousListResponse;
import sn.oas.facturation.features.rendezvous.dto.RendezVousRequest;
import sn.oas.facturation.features.rendezvous.dto.RendezVousResponse;
import sn.oas.facturation.features.rendezvous.dto.RendezVousCreationResult;
import sn.oas.facturation.features.rendezvous.dto.DemandeDerogationPlafondResponse;
import sn.oas.facturation.features.rendezvous.dto.DecisionDerogationPlafondRequest;
import sn.oas.facturation.features.rendezvous.service.RendezVousService;
import sn.oas.facturation.shared.exception.BadRequestException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rendez-vous")
@RequiredArgsConstructor
@Tag(name = "Rendez-vous", description = "API pour la gestion des rendez-vous")
public class RendezVousController {

    private final RendezVousService rendezvousService;
    private final ClientService clientService;

    // --- Agents / Backoffice ---
    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
    @Operation(summary = "Lister tous les rendez-vous avec pagination ou filtrer par client/statut/mot-clé (Agents/Admin)")
    public ResponseEntity<Page<RendezVousListResponse>> getAllRendezVous(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) RendezVousStatus statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<RendezVous> rdvPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            rdvPage = rendezvousService.searchRendezVous(keyword.trim(), page, size);
        } else if (clientId != null) {
            rdvPage = rendezvousService.getRendezVousByClientId(clientId, page, size);
        } else if (statut != null) {
            rdvPage = rendezvousService.getByStatut(statut, page, size);
        } else {
            rdvPage = rendezvousService.getAllRendezVous(page, size);
        }
        return ResponseEntity.ok(rdvPage.map(RendezVousListResponse::from));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
    @Operation(summary = "Créer un rendez-vous depuis le backoffice/admin")
    public ResponseEntity<RendezVousCreationResult> createRendezVousByAdmin(@RequestBody RendezVousRequest request) {
        String requester = currentUsername();
        RendezVousCreationResult result = rendezvousService.createRendezVousByAdmin(request, requester, currentRole());
        return responseForCreation(result);
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
    @Operation(summary = "Lister les rendez-vous d'un client spécifique avec pagination (Agents/Admin)")
    public ResponseEntity<Page<RendezVousListResponse>> getRendezVousByClientId(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(rendezvousService.getRendezVousByClientId(clientId, page, size)
                .map(RendezVousListResponse::from));
    }

    // --- Clients uniquement ---
    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Lister les rendez-vous du client connecté avec pagination (Client uniquement)")
    public ResponseEntity<Page<RendezVousListResponse>> getClientRendezVous(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(rendezvousService.getClientRendezVous(client, page, size)
                .map(RendezVousListResponse::from));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENT','CLIENT','ROLE_AGENT','AGENT','ROLE_SUPER_AGENT','SUPER_AGENT','ROLE_MASTER','MASTER','ROLE_CHEF_ATELIER','CHEF_ATELIER','ROLE_AGENT_MAGASIN','AGENT_MAGASIN')")
    @Operation(summary = "Prendre ou créer un rendez-vous (Client ou Agents/Admin)")
    public ResponseEntity<RendezVousCreationResult> bookRendezVous(@RequestBody RendezVousRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isClient = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT") || a.getAuthority().equals("CLIENT"));

        RendezVousCreationResult result;
        if (isClient) {
            Client client = clientService.getClientConnecte();
            result = rendezvousService.bookRendezVous(client, request, currentUsername());
        } else {
            result = rendezvousService.createRendezVousByAdmin(request, currentUsername(), currentRole());
        }
        return responseForCreation(result);
    }

    @GetMapping("/derogations-plafond/en-attente")
    @PreAuthorize("hasRole('SUPER_AGENT')")
    @Operation(summary = "Lister les demandes de dérogation de plafond en attente (Super Agent uniquement)")
    public ResponseEntity<Page<DemandeDerogationPlafondResponse>> getPendingQuotaDerogations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(rendezvousService.getPendingQuotaDerogations(page, size));
    }

    @PostMapping("/derogations-plafond/{id}/approuver")
    @PreAuthorize("hasRole('SUPER_AGENT')")
    @Operation(summary = "Approuver une demande de dérogation de plafond (Super Agent uniquement)")
    public ResponseEntity<RendezVousResponse> approveQuotaDerogation(
            @PathVariable Long id, @RequestBody DecisionDerogationPlafondRequest request) {
        RendezVous rv = rendezvousService.approveQuotaDerogation(id, currentUsername(), request.motif());
        return ResponseEntity.ok(RendezVousResponse.of(rv));
    }

    @PostMapping("/derogations-plafond/{id}/refuser")
    @PreAuthorize("hasRole('SUPER_AGENT')")
    @Operation(summary = "Refuser une demande de dérogation de plafond (Super Agent uniquement)")
    public ResponseEntity<DemandeDerogationPlafondResponse> rejectQuotaDerogation(
            @PathVariable Long id, @RequestBody DecisionDerogationPlafondRequest request) {
        return ResponseEntity.ok(rendezvousService.rejectQuotaDerogation(id, currentUsername(), request.motif()));
    }

    private ResponseEntity<RendezVousCreationResult> responseForCreation(RendezVousCreationResult result) {
        HttpStatus status = result.type() == RendezVousCreationResult.Type.DEROGATION_EN_ATTENTE
                ? HttpStatus.ACCEPTED : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(result);
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "inconnu";
    }

    private String currentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return "AGENT";
        return auth.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .filter(role -> !"CLIENT".equals(role))
                .findFirst()
                .orElse("AGENT");
    }

    @PutMapping("/{id}/annuler")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Annuler un rendez-vous (Client uniquement)")
    public ResponseEntity<RendezVousResponse> cancelRendezVous(@PathVariable Long id,
            @RequestParam(required = false) String motifAnnulation) {
        Client client = clientService.getClientConnecte();
        RendezVous rv = rendezvousService.cancelRendezVous(client, id, motifAnnulation);
        return ResponseEntity.ok(RendezVousResponse.of(rv));
    }

    // --- Agents / Backoffice uniquement ---
    @PutMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
    @Operation(summary = "Mettre à jour le statut d'un rendez-vous (Agents/Admin)")
    public ResponseEntity<RendezVousResponse> updateRendezVousStatus(
            @PathVariable Long id,
            @RequestParam RendezVousStatus statut,
            @RequestParam(required = false) String commentaire,
            @RequestParam(required = false) String motifAnnulation) {
        RendezVous rv = rendezvousService.updateRendezVousStatus(id, statut, commentaire, motifAnnulation);
        return ResponseEntity.ok(RendezVousResponse.of(rv));
    }

    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
    @Operation(summary = "Valider un rendez-vous et créer une fiche atelier (Agents/Admin)")
    public ResponseEntity<RendezVousResponse> validerRendezVous(
            @PathVariable Long id,
            @RequestBody List<Long> mecanicienIds) {
        RendezVous rv = rendezvousService.validerRendezVous(id, mecanicienIds);
        return ResponseEntity.ok(RendezVousResponse.of(rv));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
    @Operation(summary = "Modifier un rendez-vous (Agents/Admin, même après validation)")
    public ResponseEntity<RendezVousResponse> updateRendezVous(
            @PathVariable Long id,
            @RequestBody RendezVousRequest request) {
        RendezVous rv = rendezvousService.updateRendezVous(id, request);
        return ResponseEntity.ok(RendezVousResponse.of(rv));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
    @Operation(summary = "Modifier partiellement un rendez-vous (Agents/Admin, même après validation)")
    public ResponseEntity<RendezVousResponse> patchRendezVous(
            @PathVariable Long id,
            @RequestBody RendezVousRequest request) {
        return updateRendezVous(id, request);
    }

    @PutMapping("/{id}/date")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
    @Operation(summary = "Modifier la date d'un rendez-vous (Agents/Admin, même après validation)")
    public ResponseEntity<RendezVousResponse> updateDate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Object rawDate = body.get("nouvelleDate");
        if (rawDate == null) {
            rawDate = body.get("dateRendezVous");
        }
        if (rawDate == null) {
            rawDate = body.get("date");
        }
        if (rawDate == null) {
            rawDate = body.get("newDate");
        }
        if (rawDate == null) {
            throw new BadRequestException("La date de rendez-vous est obligatoire");
        }
        LocalDateTime nouvelleDate = parseDateTime(rawDate.toString());
        RendezVous rv = rendezvousService.updateDate(id, nouvelleDate);
        return ResponseEntity.ok(RendezVousResponse.of(rv));
    }

    @PatchMapping("/{id}/date")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
    @Operation(summary = "Modifier la date d'un rendez-vous via PATCH (Agents/Admin, même après validation)")
    public ResponseEntity<RendezVousResponse> patchDate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        return updateDate(id, body);
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            throw new BadRequestException("La date de rendez-vous est obligatoire");
        }
        dateStr = dateStr.trim();
        if (dateStr.startsWith("\"") && dateStr.endsWith("\"") && dateStr.length() > 2) {
            dateStr = dateStr.substring(1, dateStr.length() - 1).trim();
        }
        try {
            return java.time.OffsetDateTime.parse(dateStr).toLocalDateTime();
        } catch (Exception e) {
            try {
                if (dateStr.length() == 16) {
                    return LocalDateTime.parse(dateStr + ":00");
                }
                if (dateStr.contains(" ") && !dateStr.contains("T")) {
                    dateStr = dateStr.replace(" ", "T");
                    if (dateStr.length() == 16) {
                        return LocalDateTime.parse(dateStr + ":00");
                    }
                }
                if (dateStr.length() == 10) {
                    return java.time.LocalDate.parse(dateStr).atTime(8, 0);
                }
                return LocalDateTime.parse(dateStr);
            } catch (Exception ex) {
                throw new BadRequestException("Format de date invalide : " + dateStr);
            }
        }
    }

    // --- Consultation globale ---
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ROLE_CLIENT', 'ROLE_AGENT', 'AGENT', 'ROLE_SUPER_AGENT', 'SUPER_AGENT', 'ROLE_MASTER', 'MASTER', 'ROLE_CHEF_ATELIER', 'CHEF_ATELIER', 'ROLE_AGENT_MAGASIN', 'AGENT_MAGASIN')")
    @Operation(summary = "Récupérer un rendez-vous par son ID")
    public ResponseEntity<RendezVousResponse> getRendezVousById(@PathVariable Long id) {
        RendezVous rv = rendezvousService.getById(id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isClient = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT") || a.getAuthority().equals("CLIENT"));
        if (isClient) {
            Client client = clientService.getClientConnecte();
            if (rv.getClient() == null || !rv.getClient().getId().equals(client.getId())) {
                throw new sn.oas.facturation.shared.exception.ForbiddenException("Accès non autorisé à ce rendez-vous");
            }
        }
        return ResponseEntity.ok(RendezVousResponse.of(rv));
    }
}
