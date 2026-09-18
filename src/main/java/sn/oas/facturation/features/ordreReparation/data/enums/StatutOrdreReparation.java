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
    BON_DE_COMMANDE(5, "Approv."),
    BON_DE_SORTIE(6, "Attente BS"),
    ASSIGN_TECHNICIEN(7, "Assign. Tech."),
    REPARATION(8, "Réparation"),
    PAIEMENT(9, "Paiement"),
    PRET_A_LIVRER(10, "Prêt"),
    LIVRE(11, "Livré");

    private final int etape;
    private final String label;
}
