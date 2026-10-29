package sn.oas.facturation.features.bonDeLivraison.dto;

import java.math.BigDecimal;
import java.util.List;
import sn.oas.facturation.features.bonDeSortie.dto.BonDeSortieDisponibleResponse;

public record BonDeLivraisonOrdreContextResponse(
        Long ordreReparationId,
        String ordreReparationNumero,
        Boolean tvaApplicable,
        List<BonDeSortieDisponibleResponse> bonsDeSortieValides,
        BonDeLivraisonSummary bonDeLivraison) {

    public record BonDeLivraisonSummary(
            Long id,
            String numero,
            BigDecimal montantTTC,
            Long factureId,
            String factureNumero,
            Long noteDePrixId,
            String noteDePrixNumero) {}
}
