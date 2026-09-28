package sn.oas.facturation.features.technicien.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sn.oas.facturation.features.technicien.data.entity.Technicien;

/**
 * Spécialité d'un {@link Technicien}.
 * Enum fermé (pas de texte libre) — voir spec technicien.
 */
@Getter
@RequiredArgsConstructor
public enum SpecialiteTechnicien {
    MECANIQUE_GENERALE("Mécanique générale"),
    ELECTRICITE_AUTO("Électricité auto"),
    CARROSSERIE_PEINTURE("Carrosserie & Peinture"),
    TOLERIE("Tôlerie"),
    CLIMATISATION("Climatisation"),
    DIAGNOSTIC_ELECTRONIQUE("Diagnostic électronique"),
    PNEUMATIQUE("Pneumatique");

    private final String label;

    @com.fasterxml.jackson.annotation.JsonCreator
    public static SpecialiteTechnicien fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String clean = value.trim();

        for (SpecialiteTechnicien s : values()) {
            if (s.name().equalsIgnoreCase(clean)) {
                return s;
            }
        }

        for (SpecialiteTechnicien s : values()) {
            if (s.getLabel().equalsIgnoreCase(clean)) {
                return s;
            }
        }

        String normalized = normalize(clean);
        for (SpecialiteTechnicien s : values()) {
            if (normalize(s.name()).equals(normalized) || normalize(s.getLabel()).equals(normalized)) {
                return s;
            }
        }

        return null;
    }

    private static String normalize(String str) {
        if (str == null) return "";
        return java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase()
                .replaceAll("[^A-Z0-9]", "_")
                .replaceAll("_+", "_");
    }
}
