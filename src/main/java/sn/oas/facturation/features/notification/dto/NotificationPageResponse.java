package sn.oas.facturation.features.notification.dto;

import java.util.List;

public record NotificationPageResponse(
        List<NotificationResponse> content,
        int page,
        int size,
        long totalElements,
        long totalUnread,
        boolean hasNext
) {}
