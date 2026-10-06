package sn.oas.facturation.features.auth.dto.request;

import sn.oas.facturation.features.technicien.data.enums.SpecialiteTechnicien;
import sn.oas.facturation.features.user.data.enums.Role;
import sn.oas.facturation.features.user.data.enums.TypeUser;
import sn.oas.facturation.features.client.data.enums.TypeClient;

public record RegisterRequest(
        String matricule,
        String phone,
        String username,
        String firstName,
        String lastName,
        String email,
        String password,
        String confirmPassword,
        TypeUser type,
        Role role,
        Long garageId,
        // Champs spécifiques à TypeUser.TECHNICIEN — ignorés pour CLIENT/AGENT.
        String adresse,
        SpecialiteTechnicien specialite,
        // Champs spécifiques au compte client entreprise.
        TypeClient typeClient,
        String raisonSociale,
        String numeroEntreprise,
        String emailEntreprise,
        String telephoneEntreprise,
        String adresseEntreprise
) {}
