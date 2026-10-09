package sn.oas.facturation.features.client.dto;

import sn.oas.facturation.features.client.data.enums.TypeClient;
import sn.oas.facturation.shared.validation.ValidPhone;

public record ClientUpdateRequest(
        @ValidPhone String phone,
        String firstName,
        String lastName,
        String email,
        TypeClient typeClient,
        String raisonSociale,
        String numeroEntreprise,
        String emailEntreprise,
        @ValidPhone String telephoneEntreprise,
        String adresseEntreprise
) {}
