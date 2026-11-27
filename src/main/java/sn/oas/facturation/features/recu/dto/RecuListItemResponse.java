package sn.oas.facturation.features.recu.dto;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.recu.data.entity.Recu;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Ligne des listes « reçus en cours » et « reçus payés ». */
public record RecuListItemResponse(
        Long id,
        String numeroRecu,
        Long clientId,
        String numeroClient,
        String nomClient,
        LocalDateTime dateDebut,
        LocalDateTime dateFin,
        BigDecimal montant,
        BigDecimal montantPaye,
        boolean valide
) {

    public static RecuListItemResponse from(Recu r) {
        Client c = r.getClient();
        return new RecuListItemResponse(r.getId(), r.getNumero(), c == null ? null : c.getId(), numeroClient(c),
                nomClient(c), r.getDateDebut(), r.getDateFin(), r.getMontant(), r.getMontantPaye(), r.isValide());
    }

    /** Numéro client affiché, comme dans les listes de factures : le matricule du client. */
    public static String numeroClient(Client c) {
        return c == null ? null : c.getMatricule();
    }

    static String nomClient(Client c) {
        if (c == null) {
            return null;
        }
        return ((c.getFirstName() == null ? "" : c.getFirstName()) + " "
                + (c.getLastName() == null ? "" : c.getLastName())).trim();
    }
}
