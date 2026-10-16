package sn.oas.facturation.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @Size(max = 255)
        String username,
        @NotBlank @Size(max = 200)
        String oldPassword,
        @NotBlank @Size(min = 6, max = 200)
        String newPassword
) { }
