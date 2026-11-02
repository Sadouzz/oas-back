package sn.oas.facturation.features.facture.service;

import java.util.Locale;
import java.util.Set;

import sn.oas.facturation.shared.exception.BadRequestException;

/** Invoice payment choices; tax and stamp calculations are derived from this choice. */
public final class FacturePaymentMode {
    public static final String ESPECE_TTC = "ESPECE_TTC";
    public static final String CHEQUE_TTC = "CHEQUE_TTC";
    public static final String ESPECE_HT = "ESPECE_HT";
    public static final String CHEQUE_HT = "CHEQUE_HT";

    private static final Set<String> MODES = Set.of(
            ESPECE_TTC, CHEQUE_TTC, ESPECE_HT, CHEQUE_HT,
            "VIREMENT", "SOLDE_PREPAYE", "WAVE", "ORANGE_MONEY",
            "CARTE_BANCAIRE", "CARTE", "AUTRE");

    private FacturePaymentMode() {}

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) return ESPECE_TTC;
        String value = raw.trim().toUpperCase(Locale.ROOT);
        value = switch (value) {
            case "ESPECE" -> ESPECE_TTC;
            case "CHEQUE" -> CHEQUE_TTC;
            default -> value;
        };
        if (!MODES.contains(value)) throw new BadRequestException("Le mode de paiement sélectionné n'est pas reconnu.");
        return value;
    }

    public static boolean appliquerTva(String mode) {
        return !ESPECE_HT.equals(mode) && !CHEQUE_HT.equals(mode);
    }

    public static String modeRecuParDefaut(String mode) {
        return switch (mode) {
            case ESPECE_TTC, ESPECE_HT -> "ESPECE";
            case CHEQUE_TTC, CHEQUE_HT -> "CHEQUE";
            default -> mode;
        };
    }

    public static boolean isHt(String mode) {
        return ESPECE_HT.equals(mode) || CHEQUE_HT.equals(mode);
    }
}
