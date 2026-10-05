package sn.oas.facturation.features.client.dto;

import jakarta.validation.constraints.NotBlank;
import sn.oas.facturation.features.client.data.enums.TypeClient;

public record ClientCreateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String phone,
        String email,
        String password,
        String adresse,
        TypeClient typeClient,
        String raisonSociale,
        String numeroEntreprise,
        String emailEntreprise,
        String telephoneEntreprise,
        String adresseEntreprise) {
}
