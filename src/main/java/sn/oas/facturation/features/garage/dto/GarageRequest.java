package sn.oas.facturation.features.garage.dto;

public record GarageRequest(
        String nom,
        String localite,
        String prefixe,
        String numeroFixe,
        String numeroWhatsapp,
        String email
) {
}
