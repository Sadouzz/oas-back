package sn.oas.facturation.messagerie.dto;

public record MessageRequest(
        String contenu,
        Long destinataireId,
        Long garageId
) {}
