package sn.oas.facturation.features.rendezvous.dto;

import lombok.Builder;
import sn.oas.facturation.features.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.features.rendezvous.data.enums.RendezVousStatus;

import java.time.LocalDateTime;

@Builder
public record RendezVousListResponse(
        Long id,
        String numero,
        LocalDateTime dateRendezVous,
        String clientName,
        String vehiculeImmatriculation,
        String motif,
        RendezVousStatus statut,
        Boolean hasFicheAtelier) {
    public static RendezVousListResponse from(RendezVous rv) {
        if (rv == null)
            return null;
        String clientName = "";
        if (rv.getClient() != null) {
            String firstName = rv.getClient().getFirstName() != null ? rv.getClient().getFirstName() : "";
            String lastName = rv.getClient().getLastName() != null ? rv.getClient().getLastName() : "";
            clientName = (firstName + " " + lastName).trim();
        }

        return RendezVousListResponse.builder()
                .id(rv.getId())
                .numero(rv.getNumero())
                .dateRendezVous(rv.getDateRendezVous())
                .clientName(clientName)
                .vehiculeImmatriculation(rv.getVehicule() != null ? rv.getVehicule().getImmatriculation() : null)
                .motif(rv.getMotif())
                .statut(rv.getStatut())
                .hasFicheAtelier(rv.getFicheAtelier() != null)
                .build();
    }
}
