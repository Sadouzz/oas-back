package sn.oas.facturation.features.dashboard.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BonDeSortieEnAttenteDTO(
        Long id,
        String reference,
        String immatriculation,
        String clientNom,
        int nombrePieces,
        LocalDateTime date
) {}
