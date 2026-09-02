package sn.oas.facturation.features.notification.dto;

import sn.oas.facturation.features.notification.data.entity.AgentNotification;

import java.time.LocalDateTime;

public record AgentNotificationResponse(
        Long id,
        String titre,
        String message,
        boolean lu,
        LocalDateTime dateCreation
) {
    public static AgentNotificationResponse of(AgentNotification n) {
        return new AgentNotificationResponse(
                n.getId(),
                n.getTitre(),
                n.getMessage(),
                n.isLu(),
                n.getDateCreation()
        );
    }
}
