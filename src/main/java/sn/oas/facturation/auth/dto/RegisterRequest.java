package sn.oas.facturation.auth.dto;

import sn.oas.facturation.auth.data.enums.Role;
import sn.oas.facturation.auth.data.enums.TypeClient;
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
        TypeClient typeClient,
        String raisonSociale,
        String numeroEntreprise,
        String emailEntreprise,
        String adresseEntreprise,
        Long garageId
) {}
