package sn.oas.facturation.features.auth.dto;

public record ChangePasswordRequest(
        String username,
        String oldPassword,
        String newPassword
) { }
