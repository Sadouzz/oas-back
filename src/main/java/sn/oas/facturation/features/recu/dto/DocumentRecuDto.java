package sn.oas.facturation.features.recu.dto;

import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.features.recu.data.enums.TypeDocumentRecu;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Facture ou note de prix rattachée à un reçu client, avec son état de paiement. */
public record DocumentRecuDto(
        TypeDocumentRecu type,
        Long id,
        String numero,
        LocalDateTime date,
        BigDecimal montantHT,
        BigDecimal montantTotal,
        BigDecimal montantPaye,
        BigDecimal resteAPayer
) {

    public static DocumentRecuDto from(Facture f) {
        BigDecimal paye = nz(f.getMontantPaye());
        return new DocumentRecuDto(TypeDocumentRecu.FACTURE, f.getId(), f.getNumero(), f.getDateCreation(),
                f.getMontantHT(), nz(f.getMontantTotal()), paye, reste(f.getResteAPayer(), f.getMontantTotal(), paye));
    }

    public static DocumentRecuDto from(NoteDePrix n) {
        BigDecimal paye = nz(n.getMontantPaye());
        return new DocumentRecuDto(TypeDocumentRecu.NOTE_DE_PRIX, n.getId(), n.getNumero(), n.getDateCreation(),
                n.getMontantHT(), nz(n.getMontantTotal()), paye, reste(n.getResteAPayer(), n.getMontantTotal(), paye));
    }

    /** Même règle que les requêtes : resteAPayer s'il est renseigné, sinon total - payé. */
    public static BigDecimal reste(BigDecimal resteAPayer, BigDecimal total, BigDecimal paye) {
        BigDecimal reste = resteAPayer != null ? resteAPayer : nz(total).subtract(nz(paye));
        return reste.max(BigDecimal.ZERO);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
