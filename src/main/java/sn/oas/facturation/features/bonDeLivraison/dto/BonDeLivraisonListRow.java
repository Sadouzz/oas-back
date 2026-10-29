package sn.oas.facturation.features.bonDeLivraison.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BonDeLivraisonListRow(
        Long id,
        String numero,
        LocalDateTime dateCreation,
        Long ordreReparationId,
        String ordreReparationNumero,
        Long clientId,
        String clientNom,
        Long vehiculeId,
        String immatriculation,
        Boolean tvaApplicable,
        BigDecimal montantHT,
        BigDecimal montantTVA,
        BigDecimal montantTTC,
        String remarque,
        Long factureId,
        String factureNumero,
        Long noteDePrixId,
        String noteDePrixNumero) {}
