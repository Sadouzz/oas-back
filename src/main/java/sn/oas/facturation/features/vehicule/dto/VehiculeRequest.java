package sn.oas.facturation.features.vehicule.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record VehiculeRequest(
        @Size(max = 24) String immatriculation,
        @Min(1886)
        Integer annee,
        @Size(max = 100) String modele,
        @Size(max = 100) String marque,
        @PositiveOrZero Double kilometrage,
        @Size(max = 64) String numeroChassis,
        Long clientId
) {}
