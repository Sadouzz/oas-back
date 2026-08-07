package sn.oas.facturation.rendezvous.dto;

import sn.oas.facturation.rendezvous.data.entity.RendezVousDateHistory;

import java.time.LocalDateTime;

public record RendezVousDateHistoryResponse(
        Long id,
        LocalDateTime ancienneDate,
        LocalDateTime nouvelleDate,
        LocalDateTime dateModification
) {
    public static RendezVousDateHistoryResponse of(RendezVousDateHistory history) {
        return new RendezVousDateHistoryResponse(
                history.getId(),
                history.getAncienneDate(),
                history.getNouvelleDate(),
                history.getDateModification()
        );
    }
}