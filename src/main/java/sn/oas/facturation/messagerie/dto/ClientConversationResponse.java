package sn.oas.facturation.messagerie.dto;

public record ClientConversationResponse(
        Long clientId,
        String clientName,
        String clientPhone,
        MessageResponse lastMessage,
        long unreadCount
) {}
