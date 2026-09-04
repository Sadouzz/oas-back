package sn.oas.facturation.features.user.dto.response;

import lombok.Builder;
import sn.oas.facturation.features.technicien.data.entity.Technicien;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.user.data.enums.TypeUser;

import java.time.LocalDateTime;

@Builder
public record UserListResponse(
        Long id,
        String matricule,
        String username,
        String firstName,
        String lastName,
        String email,
        String phone,
        TypeUser type,
        String role,
        boolean enabled,
        // LocalDateTime createdAt,
        GarageSummary garage) {

    public record GarageSummary(Long id, String nom) {
    }

    public static UserListResponse from(User u) {
        if (u == null)
            return null;

        GarageSummary garageSummary = null;
        String role = null;

        if (u instanceof Agent agent) {
            role = agent.getRole() != null ? agent.getRole().name() : "AGENT";
            if (agent.getGarage() != null) {
                garageSummary = new GarageSummary(agent.getGarage().getId(), agent.getGarage().getNom());
            }
        } else if (u instanceof Technicien tech) {
            role = "TECHNICIEN";
            if (tech.getGarage() != null) {
                garageSummary = new GarageSummary(tech.getGarage().getId(), tech.getGarage().getNom());
            }
        } else if (u.getType() != null) {
            role = u.getType().name();
        }

        return UserListResponse.builder()
                .id(u.getId())
                .matricule(u.getMatricule())
                .username(u.getUsername())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .type(u.getType())
                .role(role)
                .enabled(u.isEnabled())
                // .createdAt(u.getCreatedAt())
                .garage(garageSummary)
                .build();
    }
}
