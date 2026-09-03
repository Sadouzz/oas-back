package sn.oas.facturation.features.proforma.dto;

import lombok.Builder;
import sn.oas.facturation.features.proforma.data.entity.Proforma;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ProformaListResponse(
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
        String statut,
        Boolean visibleClient,
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
        String numeroBonDeCommande
) {
    public static ProformaListResponse from(Proforma p) {
        if (p == null) return null;
        Long clientId = null;
        String clientNom = null;
        Long vehiculeId = null;
        String immatriculation = null;
        String numeroChassis = null;
        String marque = null;
        String modele = null;
        Integer annee = null;

        if (p.getOrdreReparation() != null && p.getOrdreReparation().getVehicule() != null) {
            var vehicule = p.getOrdreReparation().getVehicule();
            vehiculeId = vehicule.getId();
            immatriculation = vehicule.getImmatriculation();
            numeroChassis = vehicule.getNumeroChassis();
            marque = vehicule.getMarque();
            modele = vehicule.getModele();
            annee = vehicule.getAnnee();
            if (vehicule.getClient() != null) {
                clientId = vehicule.getClient().getId();
                clientNom = (vehicule.getClient().getFirstName() + " " + vehicule.getClient().getLastName()).trim();
            }
        }

        return ProformaListResponse.builder()
                .id(p.getId())
                .numero(p.getNumero())
                .dateCreation(p.getDateCreation())
                .dateModification(p.getDateModification())
                .montantHT(p.getMontantHT())
                .montantTVA(p.getMontantTVA())
                .montantTTC(p.getMontantTTC())
                .montantTimbre(p.getMontantTimbre())
                .montantAutre(BigDecimal.ZERO)
                .montantTotal(p.getMontantTotal())
                .statut(p.getStatut() != null ? p.getStatut().name() : null)
                .visibleClient(p.getVisibleClient() != null ? p.getVisibleClient() : Boolean.FALSE)
                .agentId(p.getAgent() != null ? p.getAgent().getId() : null)
                .agentNom(p.getAgent() != null ? (p.getAgent().getFirstName() + " " + p.getAgent().getLastName()).trim() : null)
                .remarque(p.getRemarque())
                .kilometrage(p.getKilometrage())
                .clientId(clientId)
                .clientNom(clientNom)
                .vehiculeId(vehiculeId)
                .immatriculation(immatriculation)
                .numeroChassis(numeroChassis)
                .marque(marque)
                .modele(modele)
                .annee(annee)
                .numeroBonDeCommande(p.getBonDeCommande() != null ? p.getBonDeCommande().getNumero() : null)
                .build();
    }
}
