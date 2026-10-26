package sn.oas.facturation.features.client.dto;

import sn.oas.facturation.features.client.data.entity.MouvementPortefeuilleClient;
import sn.oas.facturation.features.client.data.enums.TypeMouvementPortefeuille;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MouvementPortefeuilleResponse(
        Long id, TypeMouvementPortefeuille type, BigDecimal montant, String modePaiement,
        String commentaire, Long factureId, String factureNumero, Long recuId, String recuNumero,
        Long acteurId, LocalDateTime dateCreation) {

    public static MouvementPortefeuilleResponse from(MouvementPortefeuilleClient m) {
        return new MouvementPortefeuilleResponse(m.getId(), m.getType(), m.getMontant(), m.getModePaiement(),
                m.getCommentaire(), m.getFacture() == null ? null : m.getFacture().getId(),
                m.getFacture() == null ? null : m.getFacture().getNumero(),
                m.getRecu() == null ? null : m.getRecu().getId(),
                m.getRecu() == null ? null : m.getRecu().getNumero(), m.getActeurId(), m.getDateCreation());
    }
}
