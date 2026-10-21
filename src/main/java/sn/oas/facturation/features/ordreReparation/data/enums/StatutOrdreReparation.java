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
    LIVRE(8, "Livré");

    private final int etape;
    private final String label;
}
