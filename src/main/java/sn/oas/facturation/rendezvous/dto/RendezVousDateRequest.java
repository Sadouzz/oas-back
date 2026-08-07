package sn.oas.facturation.rendezvous.dto;

import java.time.LocalDateTime;

public record RendezVousDateRequest(
        LocalDateTime nouvelleDate
) {}