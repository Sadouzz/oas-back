package sn.oas.facturation.features.ordreTravail.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatutOrdreTravail {
    RECAPITULATIF(1, "Récapitulatif"),
    BON_DE_COMMANDE(2, "Approv."),
    BON_DE_SORTIE(3, "Attente BS"),
    ASSIGN_TECHNICIEN(4, "Assign. Tech."),
    REPARATION(5, "Réparation"),
    PAIEMENT(6, "Paiement"),
    PRET_A_LIVRER(7, "Prêt"),
    LIVRE(8, "Livré");

    private final int etape;
    private final String label;
}
