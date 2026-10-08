package sn.oas.facturation.features.proforma.dto;

import lombok.Builder;
import sn.oas.facturation.features.proforma.data.entity.Proforma;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ProformaDetailsResponseDto(
        Long id,
        String numero,
        String statut,
        LocalDateTime dateCreation,
        LocalDateTime dateModification,
        String agentNom,
        String remarque,

        // Informations Client (Statiques)
        Long clientId,
        String clientNom,
        String clientTelephone,

        // Informations Véhicule (Statiques)
        Long vehiculeId,
        String immatriculation,
        String numeroChassis,
        String marque,
        String modele,
        Integer annee,
        Integer kilometrage,
        String numeroBonDeCommande,

        // Données financières
        Double tvaRate,
        Double montantTimbre,
        Double montantAutre,
        Double montantHT,
        Double montantTVA,
        Double montantTTC,
        Double montantTotal,
        Double tauxRemiseClient,
        Double montantRemiseClient,

        // Lignes d'articles modifiables
        List<LignePieceResponseDto> lignesPieces,
        List<LigneMainDoeuvreResponseDto> lignesMainDoeuvres
) {

    @Builder
    public record LignePieceResponseDto(
            Long id,
            Long pieceId,             // null si PDS
            String type,              // "PDP", "PDG", "PDS"
            String referencePiece,
            String designationPiece,
            String designationPds,    // si pièce personnalisée hors stock
            Boolean isCustom,         // true si PDS
            Integer quantite,
            Double prix,              // Prix unitaire
            Double montantTotal
    ) {}

    @Builder
    public record LigneMainDoeuvreResponseDto(
            Long id,
            Long mainDoeuvreId,
            String descriptionMainDoeuvre,
            Double nbreHeure,         // Quantité / Heures
            Double tarifHoraire,      // Tarif unitaire
            Double montantTotal
    ) {}

    public static ProformaDetailsResponseDto from(Proforma p) {
        if (p == null) return null;

        Long clientId = null;
        String clientNom = null;
        String clientTelephone = null;

        Long vehiculeId = null;
        String immatriculation = null;
        String numeroChassis = null;
        String marque = null;
        String modele = null;
        Integer annee = null;
        Integer kilometrage = p.getKilometrage() != null ? p.getKilometrage().intValue() : null;

        var vehicule = p.getVehicule();
        if (vehicule == null && p.getOrdreReparation() != null) vehicule = p.getOrdreReparation().getVehicule();
        if (vehicule != null) {
            vehiculeId = vehicule.getId();
            immatriculation = vehicule.getImmatriculation();
            numeroChassis = vehicule.getNumeroChassis();
            marque = vehicule.getMarque();
            modele = vehicule.getModele();
            annee = vehicule.getAnnee();
            if (kilometrage == null && vehicule.getKilometrage() != null) {
                kilometrage = vehicule.getKilometrage().intValue();
            }
            var client = p.getClient() != null ? p.getClient() : vehicule.getClient();
            if (client != null) {
                clientId = client.getId();
                clientNom = ((client.getFirstName() != null ? client.getFirstName() : "") + " " +
                        (client.getLastName() != null ? client.getLastName() : "")).trim();
                clientTelephone = client.getPhone();
            }
        }

        double ht = p.getMontantHT() != null ? p.getMontantHT().doubleValue() : 0.0;
        double tva = p.getMontantTVA() != null ? p.getMontantTVA().doubleValue() : 0.0;
        double ttc = p.getMontantTTC() != null ? p.getMontantTTC().doubleValue() : 0.0;
        double timbre = p.getMontantTimbre() != null ? p.getMontantTimbre().doubleValue() : 0.0;
        double total = p.getMontantTotal() != null ? p.getMontantTotal().doubleValue() : (ttc + timbre);
        double tvaRate = (ht > 0) ? Math.round((tva / ht * 100.0) * 100.0) / 100.0 : 18.0;

        List<LignePieceResponseDto> pieces = p.getLignesFacturationPieces() == null ? List.of() :
                p.getLignesFacturationPieces().stream().map(lp -> {
                    Long pieceId = lp.getPiece() != null ? lp.getPiece().getId() : null;
                    String type = Boolean.TRUE.equals(lp.getIsCustom()) ? "PDS" :
                            (lp.getPiece() != null && lp.getPiece().getType() != null ? lp.getPiece().getType().name() : "PDP");
                    String ref = lp.getPiece() != null ? lp.getPiece().getReference() : null;
                    String designation = lp.getPiece() != null ? lp.getPiece().getDesignation() : lp.getDesignationPds();
                    int qte = lp.getQuantite() != null ? lp.getQuantite() : 1;
                    double prix = lp.getPrix() != null ? lp.getPrix().doubleValue() : 0.0;
                    double montantLigne = qte * prix;

                    return LignePieceResponseDto.builder()
                            .id(lp.getId())
                            .pieceId(pieceId)
                            .type(type)
                            .referencePiece(ref)
                            .designationPiece(designation)
                            .designationPds(lp.getDesignationPds())
                            .isCustom(lp.getIsCustom())
                            .quantite(qte)
                            .prix(prix)
                            .montantTotal(montantLigne)
                            .build();
                }).toList();

        List<LigneMainDoeuvreResponseDto> mainDoeuvres = p.getLignesFacturationMainDoeuvres() == null ? List.of() :
                p.getLignesFacturationMainDoeuvres().stream().map(lm -> {
                    Long moId = lm.getMainDoeuvre() != null ? lm.getMainDoeuvre().getId() : null;
                    String desc = lm.getMainDoeuvre() != null ?
                            (lm.getMainDoeuvre().getCategorie() != null ? lm.getMainDoeuvre().getCategorie().getNom() : lm.getMainDoeuvre().getDescription()) : null;
                    double heures = lm.getNbreHeure() != null ? lm.getNbreHeure().doubleValue() : 0.0;
                    double tarif = lm.getTarifHoraire() != null ? lm.getTarifHoraire().doubleValue() : 0.0;
                    double montantLigne = heures * tarif;

                    return LigneMainDoeuvreResponseDto.builder()
                            .id(lm.getId())
                            .mainDoeuvreId(moId)
                            .descriptionMainDoeuvre(desc)
                            .nbreHeure(heures)
                            .tarifHoraire(tarif)
                            .montantTotal(montantLigne)
                            .build();
                }).toList();

        String agentNom = null;
        if (p.getAgent() != null) {
            agentNom = ((p.getAgent().getFirstName() != null ? p.getAgent().getFirstName() : "") + " " +
                    (p.getAgent().getLastName() != null ? p.getAgent().getLastName() : "")).trim();
        }

        return ProformaDetailsResponseDto.builder()
                .id(p.getId())
                .numero(p.getNumero())
                .statut(p.getStatut() != null ? p.getStatut().name() : null)
                .dateCreation(p.getDateCreation())
                .dateModification(p.getDateModification())
                .agentNom(agentNom)
                .remarque(p.getRemarque())
                .clientId(clientId)
                .clientNom(clientNom)
                .clientTelephone(clientTelephone)
                .vehiculeId(vehiculeId)
                .immatriculation(immatriculation)
                .numeroChassis(numeroChassis)
                .marque(marque)
                .modele(modele)
                .annee(annee)
                .kilometrage(kilometrage)
                .numeroBonDeCommande(p.getBonDeCommande() != null ? p.getBonDeCommande().getNumero() : null)
                .tvaRate(tvaRate)
                .montantTimbre(timbre)
                .montantAutre(0.0)
                .montantHT(ht)
                .montantTVA(tva)
                .montantTTC(ttc)
                .montantTotal(total)
                .tauxRemiseClient(p.getTauxRemiseClient() == null ? 0.0 : p.getTauxRemiseClient().doubleValue())
                .montantRemiseClient(p.getMontantRemiseClient() == null ? 0.0 : p.getMontantRemiseClient().doubleValue())
                .lignesPieces(pieces)
                .lignesMainDoeuvres(mainDoeuvres)
                .build();
    }
}
