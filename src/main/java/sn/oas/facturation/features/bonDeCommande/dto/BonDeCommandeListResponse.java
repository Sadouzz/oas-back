package sn.oas.facturation.features.bonDeCommande.dto;

import lombok.Builder;
import sn.oas.facturation.features.bonDeCommande.data.entity.BonDeCommande;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record BonDeCommandeListResponse(
        Long id,
        String numero,
        LocalDateTime dateCommande,
        String statut,
        Long fournisseurId,
        String fournisseurNom,
        Long vehiculeId,
        String immatriculationVehicule,
        BigDecimal montantHT,
        BigDecimal montantTVA,
        BigDecimal montantTTC,
        Boolean tvaApplicable,
        // Boolean paye,
        String observation
) {
    public static BonDeCommandeListResponse from(BonDeCommande bc) {
        if (bc == null) return null;
        return BonDeCommandeListResponse.builder()
                .id(bc.getId())
                .numero(bc.getNumero())
                .dateCommande(bc.getDateCommande())
                .statut(bc.getStatut() != null ? bc.getStatut().name() : null)
                .fournisseurId(bc.getFournisseur() != null ? bc.getFournisseur().getId() : null)
                .fournisseurNom(bc.getFournisseur() != null ? bc.getFournisseur().getNomEntreprise() : null)
                .vehiculeId(bc.getVehicule() != null ? bc.getVehicule().getId() : null)
                .immatriculationVehicule(bc.getVehicule() != null ? bc.getVehicule().getImmatriculation() : null)
                .montantHT(bc.getMontantHT())
                .montantTVA(bc.getMontantTVA())
                .montantTTC(bc.getMontantTTC())
                .tvaApplicable(bc.getTvaApplicable())
                // .paye(bc.getPaye())
                .observation(bc.getObservation())
                .build();
    }
}
