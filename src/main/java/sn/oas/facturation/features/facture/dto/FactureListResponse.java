package sn.oas.facturation.features.facture.dto;

import lombok.Builder;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record FactureListResponse(
        Long id,
        String numero,
        LocalDateTime dateCreation,
        LocalDateTime dateModification,
        BigDecimal montantHT,
        BigDecimal montantTVA,
        BigDecimal montantTTC,
        BigDecimal montantTimbre,
        BigDecimal montantAutre,
        BigDecimal montantTotal,
        BigDecimal montantPaye,
        BigDecimal resteAPayer,
        StatutPaiement statutPaiement,
        Long agentId,
        String agentNom,
        String remarque,
        Double kilometrage,
        Long clientId,
        String clientNom,
        Long vehiculeId,
        String immatriculation,
        String numeroChassis,
        String marque,
        String modele,
        Integer annee,
        String numeroBonDeCommande,
        Long ordreReparationId,
        String numeroOrdreReparation
) {
    public static FactureListResponse from(Facture f) {
        if (f == null) return null;
        return FactureListResponse.builder()
                .id(f.getId())
                .numero(f.getNumero())
                .dateCreation(f.getDateCreation())
                .dateModification(f.getDateModification())
                .montantHT(f.getMontantHT())
                .montantTVA(f.getMontantTVA())
                .montantTTC(f.getMontantTTC())
                .montantTimbre(f.getMontantTimbre())
                .montantAutre(f.getMontantAutre())
                .montantTotal(f.getMontantTotal())
                .montantPaye(f.getMontantPaye())
                .resteAPayer(f.getResteAPayer())
                .statutPaiement(f.getStatutPaiement())
                .agentId(f.getAgent() != null ? f.getAgent().getId() : null)
                .agentNom(f.getAgent() != null ? (f.getAgent().getFirstName() + " " + f.getAgent().getLastName()).trim() : null)
                .remarque(f.getRemarque())
                .kilometrage(f.getKilometrage())
                .clientId(f.getClient() != null ? f.getClient().getId() : null)
                .clientNom(f.getClient() != null ? (f.getClient().getFirstName() + " " + f.getClient().getLastName()).trim() : null)
                .vehiculeId(f.getVehicule() != null ? f.getVehicule().getId() : null)
                .immatriculation(f.getVehicule() != null ? f.getVehicule().getImmatriculation() : null)
                .numeroChassis(f.getVehicule() != null ? f.getVehicule().getNumeroChassis() : null)
                .marque(f.getVehicule() != null ? f.getVehicule().getMarque() : null)
                .modele(f.getVehicule() != null ? f.getVehicule().getModele() : null)
                .annee(f.getVehicule() != null ? f.getVehicule().getAnnee() : null)
                .numeroBonDeCommande(f.getNumeroBonDeCommande())
                .ordreReparationId(f.getOrdreReparation() != null ? f.getOrdreReparation().getId() : null)
                .numeroOrdreReparation(f.getOrdreReparation() != null ? f.getOrdreReparation().getNumero() : null)
                .build();
    }
}
