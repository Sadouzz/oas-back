package sn.oas.facturation.auth.dto;

import sn.oas.facturation.auth.data.enums.Role;
import sn.oas.facturation.auth.data.enums.TypeUser;

public record UserUpdateRequest(
        String phone,
        String firstName,
        String lastName,
        String email,
        Role role,
        Long garageId
) {}
