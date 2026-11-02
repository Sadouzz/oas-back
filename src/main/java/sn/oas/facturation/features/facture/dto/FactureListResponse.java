package sn.oas.facturation.features.facture.dto;

import lombok.Builder;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record FactureListResponse(
        Long id,
        String numero,
        LocalDateTime dateCreation,
        // LocalDateTime dateModification,
        BigDecimal montantHT,
        BigDecimal montantTVA,
        Boolean tvaApplicable,
        BigDecimal montantTTC,
        BigDecimal montantTimbre,
        BigDecimal timbreFiscalRate,
        String modePaiement,
        BigDecimal montantAutre,
        BigDecimal montantTotal,
        BigDecimal tauxRemiseClient,
        BigDecimal montantRemiseClient,
        java.time.LocalDateTime dateEcheance,
        BigDecimal montantPaye,
        BigDecimal resteAPayer,
        StatutPaiement statutPaiement,
        // Long agentId,
        String agentNom,
        String remarque,
        Double kilometrage,
        Long clientId,
        String clientNom,
        // Long vehiculeId,
        String immatriculation,
        // String numeroChassis,
        String marque,
        String modele,
        Integer annee
        // String numeroBonDeCommande,
        // Long ordreReparationId,
        // String numeroOrdreReparation
) {
    public static FactureListResponse from(Facture f) {
        if (f == null) return null;
        var ordre = f.getOrdreReparation();
        var vehicule = f.getVehicule() != null ? f.getVehicule() : ordre != null ? ordre.getVehicule() : null;
        var client = f.getClient() != null ? f.getClient()
                : ordre != null && ordre.getClient() != null ? ordre.getClient()
                : vehicule != null ? vehicule.getClient() : null;
        return FactureListResponse.builder()
                .id(f.getId())
                .numero(f.getNumero())
                .dateCreation(f.getDateCreation())
                // .dateModification(f.getDateModification())
                .montantHT(f.getMontantHT())
                .montantTVA(f.getMontantTVA())
                .tvaApplicable(f.getTvaApplicable())
                .montantTTC(f.getMontantTTC())
                .montantTimbre(f.getMontantTimbre())
                .timbreFiscalRate(f.getTimbreFiscalRate())
                .modePaiement(f.getModePaiement())
                .montantAutre(f.getMontantAutre())
                .montantTotal(f.getMontantTotal())
                .tauxRemiseClient(f.getTauxRemiseClient())
                .montantRemiseClient(f.getMontantRemiseClient())
                .dateEcheance(f.getDateEcheance())
                .montantPaye(f.getMontantPaye())
                .resteAPayer(f.getResteAPayer())
                .statutPaiement(f.getStatutPaiement())
                // .agentId(f.getAgent() != null ? f.getAgent().getId() : null)
                .agentNom(f.getAgent() != null ? (f.getAgent().getFirstName() + " " + f.getAgent().getLastName()).trim() : null)
                .remarque(f.getRemarque())
                .kilometrage(f.getKilometrage())
                .clientId(client != null ? client.getId() : null)
                .clientNom(client != null ? (client.getFirstName() + " " + client.getLastName()).trim() : null)
                // .vehiculeId(f.getVehicule() != null ? f.getVehicule().getId() : null)
                .immatriculation(vehicule != null ? vehicule.getImmatriculation() : null)
                // .numeroChassis(f.getVehicule() != null ? f.getVehicule().getNumeroChassis() : null)
                .marque(vehicule != null ? vehicule.getMarque() : null)
                .modele(vehicule != null ? vehicule.getModele() : null)
                .annee(vehicule != null ? vehicule.getAnnee() : null)
                // .numeroBonDeCommande(f.getNumeroBonDeCommande())
                // .ordreReparationId(f.getOrdreReparation() != null ? f.getOrdreReparation().getId() : null)
                // .numeroOrdreReparation(f.getOrdreReparation() != null ? f.getOrdreReparation().getNumero() : null)
                .build();
    }
}
