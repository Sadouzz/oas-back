package sn.oas.facturation.rendezvous.dto;

import sn.oas.facturation.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.rendezvous.data.entity.RendezVousDateHistory;
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
        List<RendezVousDateHistoryResponse> dateHistory,
        String photoUrl
) {
    public static RendezVousResponse of(RendezVous rv) {
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
                List.of(),
                rv.getPhotoUrl()
        );
    }

    public static RendezVousResponse of(RendezVous rv, List<RendezVousDateHistory> history) {
        List<RendezVousDateHistoryResponse> historyResponses = history == null
                ? List.of()
                : history.stream().map(RendezVousDateHistoryResponse::of).toList();
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
                historyResponses,
                rv.getPhotoUrl()
        );
    }
}