package sn.oas.facturation.features.factureAchetee.dto;

import lombok.Builder;
import sn.oas.facturation.features.factureAchetee.data.entity.FactureAchetee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record FactureAcheteeResponse(
        Long id,
        String numeroFacture,
        LocalDate dateFacture,
        BigDecimal montantTva,
        Long agentId,
        String agentNom,
        LocalDateTime createdAt
) {
    public static FactureAcheteeResponse from(FactureAchetee f) {
        if (f == null) return null;
        String agentNom = null;
        Long agentId = null;
        if (f.getAgent() != null) {
            agentId = f.getAgent().getId();
            String fn = f.getAgent().getFirstName() != null ? f.getAgent().getFirstName().trim() : "";
            String ln = f.getAgent().getLastName() != null ? f.getAgent().getLastName().trim() : "";
            String combined = (fn + " " + ln).trim();
            agentNom = combined.isEmpty() ? null : combined;
        }
        return FactureAcheteeResponse.builder()
                .id(f.getId())
                .numeroFacture(f.getNumeroFacture())
                .dateFacture(f.getDateFacture())
                .montantTva(f.getMontantTva())
                .agentId(agentId)
                .agentNom(agentNom)
                .createdAt(f.getCreatedAt())
                .build();
    }
}
