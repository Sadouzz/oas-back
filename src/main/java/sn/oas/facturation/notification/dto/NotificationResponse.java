package sn.oas.facturation.notification.dto;

import sn.oas.facturation.notification.data.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String titre,
        String message,
        boolean lu,
        LocalDateTime dateCreation
) {
    public static NotificationResponse of(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTitre(),
                n.getMessage(),
                n.isLu(),
                n.getDateCreation()
        );
    }
}
