package sn.oas.facturation.media.dto;

public record MediaSignatureResponse(
        String signature,
        long timestamp,
        String apiKey,
        String cloudName,
        String folder
) {}
