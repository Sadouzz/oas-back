package sn.oas.facturation.vehicule.dto;

public record VehiculeRequest(
        String immatriculation,
        Integer annee,
        String modele,
        String marque,
        Double kilometrage,
        String numeroChassis,
        Long clientId
) {}
