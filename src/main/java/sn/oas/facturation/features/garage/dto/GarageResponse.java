package sn.oas.facturation.features.garage.dto;

import sn.oas.facturation.features.garage.data.entity.Garage;

import java.time.LocalDateTime;

public record GarageResponse(
        Long id,
        String nom,
        String localite,
        String prefixe,
        String numeroFixe,
        String numeroWhatsapp,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean archived
) {
    public static GarageResponse fromEntity(Garage garage) {
        if (garage == null) {
            return null;
        }
        return new GarageResponse(
                garage.getId(),
                garage.getNom(),
                garage.getLocalite(),
                garage.getPrefixe(),
                garage.getNumeroFixe(),
                garage.getNumeroWhatsapp(),
                garage.getEmail(),
                garage.getCreatedAt(),
                garage.getUpdatedAt(),
                garage.isArchived()
        );
    }
}
