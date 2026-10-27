package sn.oas.facturation.features.bonDeLivraison.dto;

import lombok.Builder;
import lombok.Value;
import sn.oas.facturation.features.bonDeLivraison.data.entity.BonDeLivraison;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Value @Builder
public class BonDeLivraisonResponse {
    Long id; String numero; LocalDateTime dateCreation; Long ordreReparationId; String ordreReparationNumero;
    Long clientId; String clientNom; Long vehiculeId; String immatriculation;
    Boolean tvaApplicable; BigDecimal montantHT; BigDecimal montantTVA; BigDecimal montantTTC;
    String remarque; List<Long> bonsDeSortieIds; List<String> bonsDeSortieReferences;
    Long factureId; String factureNumero; Long noteDePrixId; String noteDePrixNumero;

    public static BonDeLivraisonResponse from(BonDeLivraison bl) {
        var client = bl.getClient(); var vehicule = bl.getVehicule(); var ordre = bl.getOrdreReparation();
        return BonDeLivraisonResponse.builder().id(bl.getId()).numero(bl.getNumero()).dateCreation(bl.getDateCreation())
                .ordreReparationId(ordre.getId()).ordreReparationNumero(ordre.getNumero())
                .clientId(client == null ? null : client.getId())
                .clientNom(client == null ? "" : (client.getFirstName() + " " + client.getLastName()).trim())
                .vehiculeId(vehicule == null ? null : vehicule.getId())
                .immatriculation(vehicule == null ? "" : vehicule.getImmatriculation())
                .tvaApplicable(bl.getTvaApplicable()).montantHT(bl.getMontantHT()).montantTVA(bl.getMontantTVA())
                .montantTTC(bl.getMontantTTC()).remarque(bl.getRemarque())
                .bonsDeSortieIds(bl.getBonsDeSortie().stream().map(b -> b.getId()).toList())
                .bonsDeSortieReferences(bl.getBonsDeSortie().stream().map(b -> b.getReference()).toList())
                .factureId(bl.getFacture() == null ? null : bl.getFacture().getId())
                .factureNumero(bl.getFacture() == null ? null : bl.getFacture().getNumero())
                .noteDePrixId(bl.getNoteDePrix() == null ? null : bl.getNoteDePrix().getId())
                .noteDePrixNumero(bl.getNoteDePrix() == null ? null : bl.getNoteDePrix().getNumero()).build();
    }
}
