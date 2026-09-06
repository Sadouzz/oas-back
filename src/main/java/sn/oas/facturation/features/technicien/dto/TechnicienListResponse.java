package sn.oas.facturation.features.technicien.dto;

import lombok.Builder;
import sn.oas.facturation.features.technicien.data.entity.Technicien;

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
        String specialite
) {

    public static TechnicienListResponse from(Technicien t) {
        if (t == null) return null;

        return TechnicienListResponse.builder()
                .id(t.getId())
                .matricule(t.getMatricule())
                .phone(t.getPhone())
                .username(t.getUsername())
                .firstName(t.getFirstName())
                .lastName(t.getLastName())
                .email(t.getEmail())
                .adresse(t.getAdresse())
                .specialite(t.getSpecialite() != null ? t.getSpecialite().getLabel() : null)
                .build();
    }
}
