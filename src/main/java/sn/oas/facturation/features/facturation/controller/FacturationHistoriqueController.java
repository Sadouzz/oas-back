package sn.oas.facturation.features.facturation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.oas.facturation.features.facturation.data.enums.TypeHistoriqueFacturation;
import sn.oas.facturation.features.facturation.dto.FacturationHistoriqueResponseDto;
import sn.oas.facturation.features.facturation.service.FacturationHistoriqueService;
import sn.oas.facturation.shared.exception.BadRequestException;

import java.time.LocalDate;
import java.util.Locale;

@RestController
@RequestMapping("/api/facturation")
@RequiredArgsConstructor
@Tag(name = "Historique Facturation", description = "Historique agrégé des BL, Factures TTC, Avoirs TTC, Notes de prix (HT) et Avoirs HT")
public class FacturationHistoriqueController {

    private final FacturationHistoriqueService facturationHistoriqueService;

    @GetMapping("/historique")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
    @Operation(
            summary = "Récupérer l'historique des factures TTC et HT",
            description = "Agrège uniquement les BL, Factures TTC, Avoirs TTC, Notes de prix (HT) et Avoirs HT. Si une seule date est fournie (date, dateDebut seule ou dateFin seule), filtre uniquement sur cette journée ; si deux dates sont fournies (dateDebut et dateFin), filtre sur la plage.")
    public ResponseEntity<FacturationHistoriqueResponseDto> getHistorique(
            @Parameter(description = "Filtrer par type (BL, FACTURE, AVOIR_TTC, NOTE_PRIX, AVOIR_HT ou ALL)")
            @RequestParam(required = false) String type,
            @Parameter(description = "Date unique de recherche (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Date de début (YYYY-MM-DD). Utilisée seule, filtre uniquement sur ce jour.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @Parameter(description = "Date de fin (YYYY-MM-DD). Utilisée seule, filtre uniquement sur ce jour.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @Parameter(description = "Identifiant technique du client")
            @RequestParam(required = false) Long clientId,
            @Parameter(description = "Numéro client (ID, codeClient, matricule, téléphone ou nom)")
            @RequestParam(required = false) String clientNumero,
            @Parameter(description = "Mot-clé libre (numéro document, immatriculation, client, OR, statut)")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Numéro de page (0-based, optionnel)")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Taille de page (optionnel, max 500)")
            @RequestParam(required = false) Integer size) {

        TypeHistoriqueFacturation typeEnum = null;
        if (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type.trim())) {
            try {
                typeEnum = TypeHistoriqueFacturation.valueOf(type.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Type d'historique de facturation invalide : " + type);
            }
        }

        return ResponseEntity.ok(facturationHistoriqueService.getHistorique(
                typeEnum, date, dateDebut, dateFin, clientId, clientNumero, keyword, page, size));
    }
}
