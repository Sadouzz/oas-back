package sn.oas.facturation.features.messagerie.dto;

public record ClientConversationResponse(
        Long clientId,
        String clientName,
        String clientPhone,
        MessageResponse lastMessage,
        long unreadCount
) {}
