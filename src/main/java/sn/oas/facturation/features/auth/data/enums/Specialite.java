package sn.oas.facturation.features.auth.data.enums;

import sn.oas.facturation.features.auth.data.entity.Technicien;

/**
 * Spécialité d'un {@link Technicien}.
 * Enum fermé (pas de texte libre) — voir spec technicien.
 */
public enum Specialite {
    MECANIQUE_GENERALE,
    ELECTRICITE_AUTO,
    CARROSSERIE_PEINTURE,
    TOLERIE,
    CLIMATISATION,
    DIAGNOSTIC_ELECTRONIQUE,
    PNEUMATIQUE
}
