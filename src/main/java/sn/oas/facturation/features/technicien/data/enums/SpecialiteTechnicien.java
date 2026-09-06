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
}
