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
        // LocalDateTime dateModification,
        BigDecimal montantHT,
        BigDecimal montantTVA,
        Boolean tvaApplicable,
        BigDecimal montantTTC,
        BigDecimal montantTimbre,
        BigDecimal montantTotal,
        // Long agentId,
        String agentNom,
        String remarque,
        // Double kilometrage,
        Long bonDeCommandeId,
        String bonDeCommandeNumero,
        BigDecimal bonDeCommandeMontantTTC,
        String fournisseurNom,
        String statut,
        String vehiculeImmatriculation,
        String vehiculeMarque,
        String vehiculeModele,
        String clientPrenom,
        String clientNom,
        String clientTelephone
) {
    public static BonDeReceptionListResponse from(BonDeReception bl) {
        if (bl == null) return null;
        return BonDeReceptionListResponse.builder()
                .id(bl.getId())
                .numero(bl.getNumero())
                .dateCreation(bl.getDateCreation())
                // .dateModification(bl.getDateModification())
                .montantHT(bl.getMontantHT())
                .montantTVA(bl.getMontantTVA())
                .tvaApplicable(bl.getTvaApplicable())
                .montantTTC(bl.getMontantTTC())
                .montantTimbre(bl.getMontantTimbre())
                .montantTotal(bl.getMontantTotal())
                // .agentId(bl.getAgent() != null ? bl.getAgent().getId() : null)
                .agentNom(bl.getAgent() != null ? (bl.getAgent().getFirstName() + " " + bl.getAgent().getLastName()).trim() : null)
                .remarque(bl.getRemarque())
                // .kilometrage(bl.getKilometrage())
                .bonDeCommandeId(bl.getBonDeCommande() != null ? bl.getBonDeCommande().getId() : null)
                .bonDeCommandeNumero(bl.getBonDeCommande() != null ? bl.getBonDeCommande().getNumero() : null)
                .bonDeCommandeMontantTTC(bl.getBonDeCommande() != null ? bl.getBonDeCommande().getMontantTTC() : null)
                .fournisseurNom(bl.getBonDeCommande() != null && bl.getBonDeCommande().getFournisseur() != null
                        ? bl.getBonDeCommande().getFournisseur().getNom()
                        : null)
                .statut(bl.getBonDeCommande() == null || bl.getBonDeCommande().getStatut() == null ? "RECU"
                        : bl.getBonDeCommande().getStatut().name())
                .vehiculeImmatriculation(bl.getBonDeCommande() != null && bl.getBonDeCommande().getVehicule() != null
                        ? bl.getBonDeCommande().getVehicule().getImmatriculation() : null)
                .vehiculeMarque(bl.getBonDeCommande() != null && bl.getBonDeCommande().getVehicule() != null
                        ? bl.getBonDeCommande().getVehicule().getMarque() : null)
                .vehiculeModele(bl.getBonDeCommande() != null && bl.getBonDeCommande().getVehicule() != null
                        ? bl.getBonDeCommande().getVehicule().getModele() : null)
                .clientPrenom(bl.getBonDeCommande() != null && bl.getBonDeCommande().getVehicule() != null
                        && bl.getBonDeCommande().getVehicule().getClient() != null
                        ? bl.getBonDeCommande().getVehicule().getClient().getFirstName() : null)
                .clientNom(bl.getBonDeCommande() != null && bl.getBonDeCommande().getVehicule() != null
                        && bl.getBonDeCommande().getVehicule().getClient() != null
                        ? bl.getBonDeCommande().getVehicule().getClient().getLastName() : null)
                .clientTelephone(bl.getBonDeCommande() != null && bl.getBonDeCommande().getVehicule() != null
                        && bl.getBonDeCommande().getVehicule().getClient() != null
                        ? bl.getBonDeCommande().getVehicule().getClient().getPhone() : null)
                .build();
    }
}
