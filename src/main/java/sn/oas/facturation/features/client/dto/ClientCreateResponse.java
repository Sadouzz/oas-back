package sn.oas.facturation.features.client.dto;

import sn.oas.facturation.features.client.data.entity.Client;

public record ClientCreateResponse(
        Long id,
        String matricule,
        String firstName,
        String lastName,
        String phone,
        String email,
        String adresse,
        String typeClient,
        String raisonSociale,
        String numeroEntreprise,
        String emailEntreprise,
        String adresseEntreprise) {

    public static ClientCreateResponse from(Client client) {
        return new ClientCreateResponse(
                client.getId(),
                client.getMatricule(),
                client.getFirstName(),
                client.getLastName(),
                client.getPhone(),
                client.getEmail(),
                client.getAdresse(),
                client.getTypeClient() == null ? "PARTICULIER" : client.getTypeClient().name(),
                client.getRaisonSociale(),
                client.getNumeroEntreprise(),
                client.getEmailEntreprise(),
                client.getAdresseEntreprise());
    }
}
