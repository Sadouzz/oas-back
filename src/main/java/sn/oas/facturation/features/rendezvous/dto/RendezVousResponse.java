package sn.oas.facturation.features.rendezvous.dto;

import sn.oas.facturation.features.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.features.rendezvous.data.enums.RendezVousStatus;

import java.time.LocalDateTime;

public record RendezVousResponse(
        Long id,
        String numero,
        Long clientId,
        String clientName,
        Long vehiculeId,
        String vehiculeImmatriculation,
        LocalDateTime dateRendezVous,
        String motif,
        String motifAnnulation,
        RendezVousStatus statut,
        String commentaire,
        LocalDateTime dateCreation,
        boolean hasFicheAtelier) {

    public static RendezVousResponse of(RendezVous rv) {
        boolean hasFiche = rv.getFicheAtelier() != null || rv.getStatut() == RendezVousStatus.TERMINE;
        RendezVousStatus statut = hasFiche ? RendezVousStatus.TERMINE
                : rv.getStatut() == RendezVousStatus.REFUSE ? RendezVousStatus.ANNULE : rv.getStatut();
        String motifAnnulation = rv.getMotifAnnulation() != null ? rv.getMotifAnnulation()
                : rv.getStatut() == RendezVousStatus.REFUSE ? rv.getCommentaire() : null;

        return new RendezVousResponse(
                rv.getId(),
                rv.getNumero(),
                rv.getClient() != null ? rv.getClient().getId() : null,
                rv.getClient() != null ? (rv.getClient().getFirstName() + " " + rv.getClient().getLastName()).trim() : "",
                rv.getVehicule() != null ? rv.getVehicule().getId() : null,
                rv.getVehicule() != null ? rv.getVehicule().getImmatriculation() : null,
                rv.getDateRendezVous(),
                rv.getMotif(),
                motifAnnulation,
                statut,
                rv.getCommentaire(),
                rv.getDateCreation(),
                hasFiche);
    }
}
