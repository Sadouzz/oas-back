package sn.oas.facturation.auth.dto;

import sn.oas.facturation.auth.data.enums.Role;
import sn.oas.facturation.auth.data.enums.Specialite;
import sn.oas.facturation.auth.data.enums.TypeUser;

public record RegisterRequest(
        String matricule,
        String phone,
        String username,
        String firstName,
        String lastName,
        String email,
        String password,
        TypeUser type,
        Role role,
        Long garageId,
        // Champs spécifiques à TypeUser.TECHNICIEN — ignorés pour CLIENT/AGENT.
        String adresse,
        Specialite specialite
) {}
