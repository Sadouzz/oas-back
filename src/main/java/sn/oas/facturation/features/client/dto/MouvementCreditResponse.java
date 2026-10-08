package sn.oas.facturation.features.client.dto;

import sn.oas.facturation.features.client.data.entity.MouvementCreditClient;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MouvementCreditResponse(Long id, BigDecimal montant, String commentaire, Long acteurId, LocalDateTime dateCreation) {
    public static MouvementCreditResponse from(MouvementCreditClient m) {
        return new MouvementCreditResponse(m.getId(), m.getMontant(), m.getCommentaire(), m.getActeurId(), m.getDateCreation());
    }
}
