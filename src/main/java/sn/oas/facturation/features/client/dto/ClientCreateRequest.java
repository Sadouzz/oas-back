package sn.oas.facturation.features.client.dto;

import jakarta.validation.constraints.NotBlank;

public record ClientCreateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String phone,
        String email,
        String password,
        String adresse) {
}
