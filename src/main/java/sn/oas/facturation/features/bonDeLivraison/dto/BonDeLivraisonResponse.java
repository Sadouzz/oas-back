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
    List<LignePieceBL> lignesPieces; List<LigneMainDoeuvreBL> lignesMainDoeuvres;
    Long factureId; String factureNumero; Long noteDePrixId; String noteDePrixNumero;

    public record LignePieceBL(Long id, Long pieceId, String type, String designation, String reference,
            Integer quantite, Integer prix, Double tauxRemise, List<LocalDateTime> datesHeuresSaisieSource) {}
    public record LigneMainDoeuvreBL(Long id, Long mainDoeuvreId, String designation,
            Integer nbreHeure, Integer tarifHoraire, Double tauxRemise) {}

    public static BonDeLivraisonResponse from(BonDeLivraison bl) { return from(bl, true); }

    public static BonDeLivraisonResponse from(BonDeLivraison bl, boolean inclureLignes) {
        var client = bl.getClient(); var vehicule = bl.getVehicule(); var ordre = bl.getOrdreReparation();
        var builder = BonDeLivraisonResponse.builder().id(bl.getId()).numero(bl.getNumero()).dateCreation(bl.getDateCreation())
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
                .noteDePrixNumero(bl.getNoteDePrix() == null ? null : bl.getNoteDePrix().getNumero());
        if (inclureLignes) {
            builder.lignesPieces(bl.getLignesFacturationPieces() == null ? List.of() : bl.getLignesFacturationPieces().stream()
                    .map(l -> new LignePieceBL(l.getId(), l.getPiece() == null ? null : l.getPiece().getId(),
                            l.getPiece() == null || l.getPiece().getType() == null ? "PDS" : l.getPiece().getType().name(),
                            l.getPiece() == null ? l.getDesignationPds() : l.getPiece().getDesignation(),
                            l.getPiece() == null ? null : l.getPiece().getReference(), l.getQuantite(), l.getPrix(),
                            l.getTauxRemise() == null ? 0d : l.getTauxRemise().doubleValue(),
                            l.getDatesHeuresSaisieSource() == null ? List.of() : List.copyOf(l.getDatesHeuresSaisieSource())))
                    .toList());
            builder.lignesMainDoeuvres(bl.getLignesFacturationMainDoeuvres() == null ? List.of() : bl.getLignesFacturationMainDoeuvres().stream()
                    .map(l -> new LigneMainDoeuvreBL(l.getId(), l.getMainDoeuvre() == null ? null : l.getMainDoeuvre().getId(),
                            l.getMainDoeuvre() == null ? "Main-d'œuvre" : l.getMainDoeuvre().getDescription(),
                            l.getNbreHeure(), l.getTarifHoraire(), l.getTauxRemise() == null ? 0d : l.getTauxRemise().doubleValue()))
                    .toList());
        }
        return builder.build();
    }

    public static BonDeLivraisonResponse fromListRow(BonDeLivraisonListRow row,
            List<BonDeLivraisonSortieReference> sorties) {
        List<BonDeLivraisonSortieReference> references = sorties == null ? List.of() : sorties;
        return BonDeLivraisonResponse.builder()
                .id(row.id()).numero(row.numero()).dateCreation(row.dateCreation())
                .ordreReparationId(row.ordreReparationId()).ordreReparationNumero(row.ordreReparationNumero())
                .clientId(row.clientId()).clientNom(row.clientNom() == null ? "" : row.clientNom().trim())
                .vehiculeId(row.vehiculeId()).immatriculation(row.immatriculation() == null ? "" : row.immatriculation())
                .tvaApplicable(row.tvaApplicable()).montantHT(row.montantHT()).montantTVA(row.montantTVA())
                .montantTTC(row.montantTTC()).remarque(row.remarque())
                .bonsDeSortieIds(references.stream().map(BonDeLivraisonSortieReference::bonDeSortieId).toList())
                .bonsDeSortieReferences(references.stream().map(BonDeLivraisonSortieReference::reference).toList())
                .lignesPieces(List.of()).lignesMainDoeuvres(List.of())
                .factureId(row.factureId()).factureNumero(row.factureNumero())
                .noteDePrixId(row.noteDePrixId()).noteDePrixNumero(row.noteDePrixNumero())
                .build();
    }
}
