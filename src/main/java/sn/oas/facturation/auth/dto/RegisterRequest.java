package sn.oas.facturation.auth.dto;

import sn.oas.facturation.auth.data.enums.TypeUser;

public record RegisterRequest(
        String matricule,
        String phone,
        String login,
        String firstName,
        String lastName,
        String email,
        String password,
        TypeUser type
) {}
