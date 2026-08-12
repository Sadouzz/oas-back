package sn.oas.facturation.garage.dto;

public record GarageRequest(
        String nom,
        String localite,
        String prefixe,
        String numeroFixe,
        String numeroWhatsapp,
        String email
) {
}
