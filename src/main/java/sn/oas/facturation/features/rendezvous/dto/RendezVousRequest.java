package sn.oas.facturation.features.rendezvous.dto;

import sn.oas.facturation.features.rendezvous.data.enums.RendezVousStatus;

import java.time.LocalDateTime;

public record RendezVousRequest(
        LocalDateTime dateRendezVous,
        String motif,
        Long vehiculeId,
        Long garageId,
        Long clientId,
        RendezVousStatus statut,
        String commentaire
) {
    public RendezVousRequest(LocalDateTime dateRendezVous, String motif, Long vehiculeId, Long garageId) {
        this(dateRendezVous, motif, vehiculeId, garageId, null, null, null);
    }
}

