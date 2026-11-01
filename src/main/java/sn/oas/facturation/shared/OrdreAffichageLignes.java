package sn.oas.facturation.shared;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/** Helpers for displaying document lines in reverse insertion order. */
public final class OrdreAffichageLignes {
    private OrdreAffichageLignes() {}

    public static <T> List<T> plusRecentesEnPremier(List<T> lignes, Function<T, Long> identifiant) {
        if (lignes == null || lignes.isEmpty()) {
            return List.of();
        }
        return lignes.stream()
                .sorted(Comparator.comparing(identifiant, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
}
