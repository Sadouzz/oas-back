package sn.oas.facturation.features.user.dto.request;

import sn.oas.facturation.features.user.data.enums.Role;
import sn.oas.facturation.shared.validation.ValidPhone;

public record UserUpdateRequest(
        @ValidPhone String phone,
        String firstName,
        String lastName,
        String email,
        Role role,
        Long garageId
) {}
