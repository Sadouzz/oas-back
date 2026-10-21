package sn.oas.facturation.features.ordreReparation.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatutOrdreReparation {
    RECEPTION(1, "Réception"),
    DIAGNOSTIC(2, "Diagnostic"),
    PIECES_MO(3, "Pièces & MO"),
    PROFORMA(4, "Proforma"),
    ORDRE_DE_TRAVAIL(5, "Ordre de Travail"),
    PAIEMENT(6, "Paiement"),
    PRET_A_LIVRER(7, "Prêt"),
    LIVRE(8, "Livré"),

    // Valeurs historiques conservées pour lire les OR avant l'application
    // de la migration 20261021_migrer_statuts_ordre_reparation_ot.sql.
    @Deprecated BON_DE_COMMANDE(5, "Ancien statut approvisionnement"),
    @Deprecated BON_DE_SORTIE(5, "Ancien statut bon de sortie"),
    @Deprecated ASSIGN_TECHNICIEN(5, "Ancien statut affectation technicien"),
    @Deprecated REPARATION(5, "Ancien statut réparation");

    private final int etape;
    private final String label;
}
