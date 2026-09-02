package sn.oas.facturation.features.messagerie.dto;

public record MessageRequest(
        String contenu,
        Long destinataireId
) {}
