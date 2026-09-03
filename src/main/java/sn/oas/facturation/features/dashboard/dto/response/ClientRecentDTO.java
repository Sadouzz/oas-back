package sn.oas.facturation.features.dashboard.dto.response;

import lombok.Builder;

@Builder
public record ClientRecentDTO(
        Long id,
        String nom,
        String telephone
) {}
