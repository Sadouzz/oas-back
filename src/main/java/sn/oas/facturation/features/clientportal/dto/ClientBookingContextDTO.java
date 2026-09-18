package sn.oas.facturation.features.clientportal.dto;

import java.util.List;

public record ClientBookingContextDTO(
        List<VehiculeBookingDTO> vehicules,
        List<GarageBookingDTO> garages
) {
    public record VehiculeBookingDTO(
            Long id,
            String immatriculation,
            String marque,
            String modele,
            Integer annee,
            Double kilometrage,
            String numeroChassis,
            boolean disponiblePourRdv
    ) {}

    public record GarageBookingDTO(
            Long id,
            String nom,
            String localite,
            String prefixe,
            String numeroFixe,
            String numeroWhatsapp,
            String email
    ) {}
}
