package sn.oas.facturation.features.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FactureEcheanceClientResponse(
        Long factureId,
        String numero,
        LocalDateTime dateEmission,
        LocalDateTime dateEcheance,
        BigDecimal resteAPayer,
        Long ordreReparationId,
        String numeroOrdreReparation,
        String immatriculation,
        String marque,
        String modele) { }
