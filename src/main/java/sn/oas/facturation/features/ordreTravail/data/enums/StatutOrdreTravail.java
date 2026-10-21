package sn.oas.facturation.features.ordreTravail.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatutOrdreTravail {
    RECAPITULATIF(1, "Récapitulatif"),
    BON_DE_SORTIE(2, "Attente BS"),
    ASSIGN_TECHNICIEN(3, "Assign. Tech."),
    REPARATION(4, "Réparation"),
    TERMINE(5, "Terminé");

    private final int etape;
    private final String label;
}
