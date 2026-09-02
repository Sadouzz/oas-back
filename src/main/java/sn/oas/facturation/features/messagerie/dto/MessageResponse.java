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
    public static MessageResponse of(Message m) {
        return new MessageResponse(
                m.getId(),
                m.getExpediteur().getId(),
                m.getExpediteur().getFirstName() + " " + m.getExpediteur().getLastName(),
                m.getContenu(),
                m.getDateEnvoi(),
                m.isLu()
        );
    }
}
