package sn.oas.facturation.features.garage.dto;

import sn.oas.facturation.shared.validation.ValidPhone;

public record GarageRequest(
        String nom,
        String localite,
        String prefixe,
        @ValidPhone String numeroFixe,
        @ValidPhone String numeroWhatsapp,
        String email
) {
}
