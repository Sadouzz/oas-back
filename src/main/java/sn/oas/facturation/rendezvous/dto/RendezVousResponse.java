package sn.oas.facturation.rendezvous.dto;

import sn.oas.facturation.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.rendezvous.data.enums.RendezVousStatus;

import java.time.LocalDateTime;
import java.util.List;

public record RendezVousResponse(
        Long id,
        Long clientId,
        String clientName,
        Long vehiculeId,
        String vehiculeImmatriculation,
        LocalDateTime dateRendezVous,
        String motif,
        RendezVousStatus statut,
        String commentaire,
        LocalDateTime dateCreation,
        boolean hasFicheAtelier) {
    public static RendezVousResponse of(RendezVous rv, boolean hasFicheAtelier) {
        return new RendezVousResponse(
                rv.getId(),
                rv.getClient().getId(),
                rv.getClient().getFirstName() + " " + rv.getClient().getLastName(),
                rv.getVehicule() != null ? rv.getVehicule().getId() : null,
                rv.getVehicule() != null ? rv.getVehicule().getImmatriculation() : null,
                rv.getDateRendezVous(),
                rv.getMotif(),
                rv.getStatut(),
                rv.getCommentaire(),
                rv.getDateCreation(),
                hasFicheAtelier);
    }
}