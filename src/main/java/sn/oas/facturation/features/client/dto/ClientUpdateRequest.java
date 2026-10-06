package sn.oas.facturation.features.client.dto;

import sn.oas.facturation.features.client.data.enums.TypeClient;

public record ClientUpdateRequest(
        String phone,
        String firstName,
        String lastName,
        String email,
        TypeClient typeClient,
        String raisonSociale,
        String numeroEntreprise,
        String emailEntreprise,
        String telephoneEntreprise,
        String adresseEntreprise
) {}
