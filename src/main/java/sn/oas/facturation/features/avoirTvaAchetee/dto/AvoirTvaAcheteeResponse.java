package sn.oas.facturation.features.avoirTvaAchetee.dto;

import lombok.Builder;
import sn.oas.facturation.features.avoirTvaAchetee.data.entity.AvoirTvaAchetee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record AvoirTvaAcheteeResponse(
        Long id,
        String designation,
        LocalDate dateOperation,
        BigDecimal montantTva,
        Long agentId,
        String agentNom,
        LocalDateTime createdAt
) {
    public static AvoirTvaAcheteeResponse from(AvoirTvaAchetee a) {
        if (a == null) return null;
        String agentNom = null;
        Long agentId = null;
        if (a.getAgent() != null) {
            agentId = a.getAgent().getId();
            String fn = a.getAgent().getFirstName() != null ? a.getAgent().getFirstName().trim() : "";
            String ln = a.getAgent().getLastName() != null ? a.getAgent().getLastName().trim() : "";
            String combined = (fn + " " + ln).trim();
            agentNom = combined.isEmpty() ? null : combined;
        }
        return AvoirTvaAcheteeResponse.builder()
                .id(a.getId())
                .designation(a.getDesignation())
                .dateOperation(a.getDateOperation())
                .montantTva(a.getMontantTva())
                .agentId(agentId)
                .agentNom(agentNom)
                .createdAt(a.getCreatedAt())
                .build();
    }
}
