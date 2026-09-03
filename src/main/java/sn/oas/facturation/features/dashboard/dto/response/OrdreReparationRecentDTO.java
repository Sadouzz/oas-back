package sn.oas.facturation.features.dashboard.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrdreReparationRecentDTO(
        Long id,
        String numero,
        String statut,
        String immatriculation,
        LocalDateTime date
) {}
