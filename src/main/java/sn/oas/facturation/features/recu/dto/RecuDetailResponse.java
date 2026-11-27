package sn.oas.facturation.features.recu.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Page d'un reçu client : documents rattachés, totaux et versements. */
public record RecuDetailResponse(
        Long id,
        String numeroRecu,
        Long clientId,
        String numeroClient,
        String nomClient,
        String telephone,
        LocalDateTime dateDebut,
        LocalDateTime dateFin,
        boolean enCours,
        boolean valide,
        LocalDateTime dateValidation,
        List<DocumentRecuDto> factures,
        List<DocumentRecuDto> notesDePrix,
        BigDecimal totalFactures,
        BigDecimal totalNotesDePrix,
        BigDecimal montant,
        BigDecimal montantPaye,
        BigDecimal resteAPayer,
        List<VersementClientDto> versements
) {
}
