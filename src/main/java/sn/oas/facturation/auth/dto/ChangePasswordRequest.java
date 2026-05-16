package sn.oas.facturation.auth.dto;

public record ChangePasswordRequest(
        String username,
        String oldPassword,
        String newPassword
) { }
