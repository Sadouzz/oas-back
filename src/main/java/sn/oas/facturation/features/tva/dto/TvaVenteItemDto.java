package sn.oas.facturation.features.tva.dto;

import lombok.Builder;
import sn.oas.facturation.features.avoirTTC.data.entity.AvoirTTC;
import sn.oas.facturation.features.facture.data.entity.Facture;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TvaVenteItemDto(
        Long id,
        String numero,
        LocalDateTime date,
        BigDecimal montantTva,
        BigDecimal montantTimbre
) {
    public static TvaVenteItemDto fromFacture(Facture f) {
        if (f == null) return null;
        return TvaVenteItemDto.builder()
                .id(f.getId())
                .numero(f.getNumero())
                .date(f.getDateCreation())
                .montantTva(f.getMontantTVA() != null ? f.getMontantTVA() : BigDecimal.ZERO)
                .montantTimbre(f.getMontantTimbre() != null ? f.getMontantTimbre() : BigDecimal.ZERO)
                .build();
    }

    public static TvaVenteItemDto fromAvoirTTC(AvoirTTC a) {
        if (a == null) return null;
        return TvaVenteItemDto.builder()
                .id(a.getId())
                .numero(a.getNumero())
                .date(a.getDateCreation())
                .montantTva(a.getMontantTVA() != null ? a.getMontantTVA() : BigDecimal.ZERO)
                .montantTimbre(a.getMontantTimbre() != null ? a.getMontantTimbre() : BigDecimal.ZERO)
                .build();
    }
}
