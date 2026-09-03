package sn.oas.facturation.features.user.dto.request;

import sn.oas.facturation.features.user.data.enums.Role;

public record UserUpdateRequest(
        String phone,
        String firstName,
        String lastName,
        String email,
        Role role,
        Long garageId
) {}
