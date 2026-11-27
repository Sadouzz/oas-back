package sn.oas.facturation.features.recu.dto;

import sn.oas.facturation.features.recu.data.entity.LigneVersementClient;
import sn.oas.facturation.features.recu.data.entity.VersementClient;
import sn.oas.facturation.features.recu.data.enums.TypeDocumentRecu;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VersementClientDto(
        Long id,
        LocalDateTime dateVersement,
        BigDecimal montant,
        String modePaiement,
        String remarque,
        List<Imputation> imputations
) {

    /** Part du versement imputée à un document ; numeroRecuFacture (reçu RC) n'est renseigné que pour une facture. */
    public record Imputation(
            TypeDocumentRecu type,
            Long documentId,
            String numeroDocument,
            String numeroRecuFacture,
            BigDecimal montant
    ) {
        static Imputation from(LigneVersementClient l) {
            if (l.getFacture() != null) {
                return new Imputation(TypeDocumentRecu.FACTURE, l.getFacture().getId(), l.getFacture().getNumero(),
                        l.getRecuFacture() == null ? null : l.getRecuFacture().getNumero(), l.getMontant());
            }
            return new Imputation(TypeDocumentRecu.NOTE_DE_PRIX,
                    l.getNoteDePrix() == null ? null : l.getNoteDePrix().getId(),
                    l.getNoteDePrix() == null ? null : l.getNoteDePrix().getNumero(),
                    null, l.getMontant());
        }
    }

    public static VersementClientDto from(VersementClient v) {
        return new VersementClientDto(v.getId(), v.getDateVersement(), v.getMontant(), v.getModePaiement(),
                v.getRemarque(), v.getLignes().stream().map(Imputation::from).toList());
    }
}
