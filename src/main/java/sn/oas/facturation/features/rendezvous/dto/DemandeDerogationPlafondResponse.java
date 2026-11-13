package sn.oas.facturation.features.rendezvous.dto;

import sn.oas.facturation.features.rendezvous.data.entity.DemandeDerogationPlafond;
import sn.oas.facturation.features.rendezvous.data.enums.StatutDemandeDerogationPlafond;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DemandeDerogationPlafondResponse(
        Long id,
        Long clientId,
        String clientNom,
        String clientTelephone,
        Long vehiculeId,
        String vehiculeImmatriculation,
        Long garageId,
        String garageNom,
        LocalDateTime dateRendezVous,
        String motif,
        String commentaire,
        StatutDemandeDerogationPlafond statut,
        String statutRendezVousDemande,
        String origine,
        String demandeur,
        BigDecimal plafondSnapshot,
        BigDecimal montantConsommeSnapshot,
        Integer echeanceJoursSnapshot,
        LocalDateTime dateDemande,
        String decisionnaire,
        LocalDateTime dateDecision,
        String motifDecision,
        Long rendezVousId,
        String rendezVousNumero
) {
    public static DemandeDerogationPlafondResponse from(DemandeDerogationPlafond demande) {
        return new DemandeDerogationPlafondResponse(
                demande.getId(),
                demande.getClient().getId(),
                (demande.getClient().getFirstName() + " " + demande.getClient().getLastName()).trim(),
                demande.getClient().getPhone(),
                demande.getVehicule().getId(),
                demande.getVehicule().getImmatriculation(),
                demande.getGarage() != null ? demande.getGarage().getId() : null,
                demande.getGarage() != null ? demande.getGarage().getNom() : null,
                demande.getDateRendezVous(),
                demande.getMotif(),
                demande.getCommentaire(),
                demande.getStatut(),
                demande.getStatutRendezVousDemande() != null ? demande.getStatutRendezVousDemande().name() : null,
                demande.getOrigine(),
                demande.getDemandeur(),
                demande.getPlafondSnapshot(),
                demande.getMontantConsommeSnapshot(),
                demande.getEcheanceJoursSnapshot(),
                demande.getDateDemande(),
                demande.getDecisionnaire(),
                demande.getDateDecision(),
                demande.getMotifDecision(),
                demande.getRendezVous() != null ? demande.getRendezVous().getId() : null,
                demande.getRendezVous() != null ? demande.getRendezVous().getNumero() : null
        );
    }
}
