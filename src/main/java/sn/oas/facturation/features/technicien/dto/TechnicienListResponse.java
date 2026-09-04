package sn.oas.facturation.features.technicien.dto;

import lombok.Builder;
import sn.oas.facturation.features.technicien.data.entity.Technicien;
import sn.oas.facturation.features.technicien.data.enums.SpecialiteTechnicien;

import java.time.LocalDateTime;

@Builder
public record TechnicienListResponse(
        Long id,
        String matricule,
        String phone,
        String username,
        String firstName,
        String lastName,
        String email,
        String adresse,
        SpecialiteTechnicien specialite,
        // boolean enabled,
        // LocalDateTime createdAt,
        GarageSummary garage
) {
    public record GarageSummary(Long id, String nom) {}

    public static TechnicienListResponse from(Technicien t) {
        if (t == null) return null;

        GarageSummary garageSummary = null;
        if (t.getGarage() != null) {
            garageSummary = new GarageSummary(t.getGarage().getId(), t.getGarage().getNom());
        }

        return TechnicienListResponse.builder()
                .id(t.getId())
                .matricule(t.getMatricule())
                .phone(t.getPhone())
                .username(t.getUsername())
                .firstName(t.getFirstName())
                .lastName(t.getLastName())
                .email(t.getEmail())
                .adresse(t.getAdresse())
                .specialite(t.getSpecialite())
                // .enabled(t.isEnabled())
                // .createdAt(t.getCreatedAt())
                .garage(garageSummary)
                .build();
    }
}
