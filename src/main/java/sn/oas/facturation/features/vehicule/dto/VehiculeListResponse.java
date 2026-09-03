package sn.oas.facturation.features.vehicule.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;

@Builder
public record VehiculeListResponse(
        Long id,
        String immatriculation,
        Integer annee,
        String modele,
        String marque,
        Double kilometrage,
        String numeroChassis,
        ClientSummary client,
        LocalDateTime createdAt) {

    public record ClientSummary(
            Long id,
            String firstName,
            String lastName,
            String phone) {
    }

    public static VehiculeListResponse from(Vehicule v) {
        if (v == null)
            return null;

        ClientSummary clientSummary = null;
        if (v.getClient() != null) {
            clientSummary = new ClientSummary(
                    v.getClient().getId(),
                    v.getClient().getFirstName(),
                    v.getClient().getLastName(),
                    v.getClient().getPhone());
        }

        return VehiculeListResponse.builder()
                .id(v.getId())
                .immatriculation(v.getImmatriculation())
                .annee(v.getAnnee())
                .modele(v.getModele())
                .marque(v.getMarque())
                .kilometrage(v.getKilometrage())
                .numeroChassis(v.getNumeroChassis())
                .client(clientSummary)
                .createdAt(v.getCreatedAt())
                .build();
    }
}
