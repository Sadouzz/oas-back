package sn.oas.facturation.features.pdfGenerator.service;

import sn.oas.facturation.shared.exception.BadRequestException;

import java.util.Locale;

public final class PdfLayout {
    public static final String AVEC_ENTETE = "AVEC_ENTETE";
    public static final String SANS_ENTETE = "SANS_ENTETE";

    private PdfLayout() {}

    public static String normalize(String layoutKey) {
        String normalized = layoutKey == null || layoutKey.isBlank()
                ? AVEC_ENTETE : layoutKey.trim().toUpperCase(Locale.ROOT);
        if (!AVEC_ENTETE.equals(normalized) && !SANS_ENTETE.equals(normalized)) {
            throw new BadRequestException("Variante PDF invalide.");
        }
        return normalized;
    }
}
