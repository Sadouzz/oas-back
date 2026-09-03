package sn.oas.facturation.features.dashboard.dto.response;

import lombok.Builder;

@Builder
public record AlerteStockRecentDTO(
        Long id,
        String designation,
        String reference,
        Double seuil,
        Double stockMagasin,
        String statut
) {}
