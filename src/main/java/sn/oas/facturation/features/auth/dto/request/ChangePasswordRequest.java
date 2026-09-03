package sn.oas.facturation.features.auth.dto.request;

public record ChangePasswordRequest(
        String username,
        String oldPassword,
        String newPassword
) { }
