package sn.oas.facturation.rendezvous.dto;

import java.time.LocalDateTime;

public record RendezVousRequest(
        LocalDateTime dateRendezVous,
        String motif,
        Long vehiculeId,
        Long garageId
) {}
