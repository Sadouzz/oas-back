package sn.oas.facturation.features.rendezvous.dto;

import java.time.LocalDateTime;

public record RendezVousRequest(
        LocalDateTime dateRendezVous,
        String motif,
        Long vehiculeId,
        Long garageId
) {}
