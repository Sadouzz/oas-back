package sn.oas.facturation.features.bonDeReception.dto;

import lombok.Builder;
import sn.oas.facturation.features.bonDeReception.data.entity.BonDeReception;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record BonDeReceptionListResponse(
        Long id,
        String numero,
        LocalDateTime dateCreation,
        LocalDateTime dateModification,
        BigDecimal montantHT,
        BigDecimal montantTVA,
        BigDecimal montantTTC,
        BigDecimal montantTimbre,
        BigDecimal montantTotal,
        Long agentId,
        String agentNom,
        String remarque,
        Double kilometrage,
        Long bonDeCommandeId,
        String bonDeCommandeNumero
) {
    public static BonDeReceptionListResponse from(BonDeReception bl) {
        if (bl == null) return null;
        return BonDeReceptionListResponse.builder()
                .id(bl.getId())
                .numero(bl.getNumero())
                .dateCreation(bl.getDateCreation())
                .dateModification(bl.getDateModification())
                .montantHT(bl.getMontantHT())
                .montantTVA(bl.getMontantTVA())
                .montantTTC(bl.getMontantTTC())
                .montantTimbre(bl.getMontantTimbre())
                .montantTotal(bl.getMontantTotal())
                .agentId(bl.getAgent() != null ? bl.getAgent().getId() : null)
                .agentNom(bl.getAgent() != null ? (bl.getAgent().getFirstName() + " " + bl.getAgent().getLastName()).trim() : null)
                .remarque(bl.getRemarque())
                .kilometrage(bl.getKilometrage())
                .bonDeCommandeId(bl.getBonDeCommande() != null ? bl.getBonDeCommande().getId() : null)
                .bonDeCommandeNumero(bl.getBonDeCommande() != null ? bl.getBonDeCommande().getNumero() : null)
                .build();
    }
}
