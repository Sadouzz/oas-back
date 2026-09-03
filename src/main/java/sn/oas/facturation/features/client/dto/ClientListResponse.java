package sn.oas.facturation.features.client.dto;

import lombok.Builder;
import sn.oas.facturation.features.client.data.entity.Client;

import java.time.LocalDateTime;

@Builder
public record ClientListResponse(
        Long id,
        String matricule,
        String phone,
        String username,
        String firstName,
        String lastName,
        String email,
        boolean enabled,
        LocalDateTime createdAt,
        Integer vehiculeNumbers
) {
    public static ClientListResponse from(Client client) {
        if (client == null) return null;
        return ClientListResponse.builder()
                .id(client.getId())
                .matricule(client.getMatricule())
                .phone(client.getPhone())
                .username(client.getUsername())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .email(client.getEmail())
                .enabled(client.isEnabled())
                .createdAt(client.getCreatedAt())
                .vehiculeNumbers(client.getVehicules().size())
                .build();
    }
}