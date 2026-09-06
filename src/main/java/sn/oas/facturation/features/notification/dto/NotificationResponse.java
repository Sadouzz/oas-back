package sn.oas.facturation.features.notification.dto;

import sn.oas.facturation.features.notification.data.entity.AgentNotification;
import sn.oas.facturation.features.notification.data.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String titre,
        String message,
        boolean lu,
        LocalDateTime dateCreation
) {
    public static NotificationResponse of(Notification n) {
        return from(n);
    }

    public static NotificationResponse from(Notification n) {
        if (n == null) return null;
        return new NotificationResponse(
                n.getId(),
                n.getTitre(),
                n.getMessage(),
                n.isLu(),
                n.getDateCreation()
        );
    }

    public static NotificationResponse of(AgentNotification n) {
        if (n == null) return null;
        return new NotificationResponse(
                n.getId(),
                n.getTitre(),
                n.getMessage(),
                n.isLu(),
                n.getDateCreation()
        );
    }
}
