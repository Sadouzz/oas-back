package sn.oas.facturation.features.client.dto;

import jakarta.validation.constraints.NotBlank;
import sn.oas.facturation.features.client.data.enums.TypeClient;
import sn.oas.facturation.shared.validation.ValidPhone;

public record ClientCreateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @ValidPhone String phone,
        String email,
        String password,
        String adresse,
        TypeClient typeClient,
        String raisonSociale,
        String numeroEntreprise,
        String emailEntreprise,
        @ValidPhone String telephoneEntreprise,
        String adresseEntreprise) {
}
