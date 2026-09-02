package sn.oas.facturation.features.auth.dto;

import sn.oas.facturation.features.auth.data.enums.Role;

public record UserUpdateRequest(
        String phone,
        String firstName,
        String lastName,
        String email,
        Role role,
        Long garageId
) {}
