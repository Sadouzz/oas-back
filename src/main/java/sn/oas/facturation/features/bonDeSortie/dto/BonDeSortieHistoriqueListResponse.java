package sn.oas.facturation.features.bonDeSortie.dto;

import lombok.Builder;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortieHistorique;
import sn.oas.facturation.features.bonDeSortie.data.enums.StatutBon;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record BonDeSortieHistoriqueListResponse(
        Long id,
        String prenom,
        String nom,
        String numBs,
        String numeroSerie,
        String immatriculation,
        String designation,
        String action,
        Double quantite,
        Double stockMagasin,
        Double stockAtelier,
        Double qteReelle,
        LocalDateTime dateAction
// Long bonDeSortieId
) {

    public static BonDeSortieHistoriqueListResponse from(BonDeSortieHistorique h) {
        if (h == null)
            return null;
        return BonDeSortieHistoriqueListResponse.builder()
                .id(h.getId())
                .prenom(h.getPrenom())
                .nom(h.getNom())
                .numBs(h.getNumBs())
                .numeroSerie(h.getNumeroSerie())
                .immatriculation(h.getImmatriculation())
                .designation(h.getDesignation())
                .action(h.getStatut())
                .quantite(h.getQuantite())
                .stockMagasin(h.getStockMagasin())
                .stockAtelier(h.getStockAtelier())
                .qteReelle(h.getQteReelle())
                .dateAction(h.getDateAction())
                //.bonDeSortieId(h.getBonDeSortie() != null ? h.getBonDeSortie().getId() : null)
                .build();
    }
}
