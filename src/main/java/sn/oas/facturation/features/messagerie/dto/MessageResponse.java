package sn.oas.facturation.features.messagerie.dto;

import sn.oas.facturation.features.messagerie.data.entity.Message;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long expediteurId,
        String expediteurName,
        String contenu,
        LocalDateTime dateEnvoi,
        boolean lu
) {
    public static MessageResponse from(Message m) {
        return of(m);
    }

    public static MessageResponse of(Message m) {
        if (m == null) return null;
        return new MessageResponse(
                m.getId(),
                m.getExpediteur() != null ? m.getExpediteur().getId() : null,
                m.getExpediteur() != null ? (m.getExpediteur().getFirstName() + " " + m.getExpediteur().getLastName()).trim() : null,
                m.getContenu(),
                m.getDateEnvoi(),
                m.isLu()
        );
    }
}
