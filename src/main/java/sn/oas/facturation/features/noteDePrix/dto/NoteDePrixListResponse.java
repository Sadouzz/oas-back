package sn.oas.facturation.features.noteDePrix.dto;

import lombok.Builder;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record NoteDePrixListResponse(
        Long id,
        String numero,
        LocalDateTime dateCreation,
        BigDecimal montantHT,
        BigDecimal montantTotal,
        BigDecimal montantPaye,
        BigDecimal resteAPayer,
        String statutPaiement,
        String modePaiement,
        // String numeroBonDeCommande,
        // BigDecimal montantAutre,
        // Long agentId,
        String agentNom,
        // Long clientId,
        String clientNom,
        // Long vehiculeId,
        String vehiculeImmatriculation,
        String immatriculation,
        String marque,
        String modele,
        // String numeroChassis,
        Double kilometrage,
        String remarque
        // String statut,
        // Long ordreReparationId,
        // String numeroOrdreReparation
) {
    public static NoteDePrixListResponse from(NoteDePrix note) {
        if (note == null) return null;

        Long agentId = null;
        String agentNom = null;
        if (note.getAgent() != null) {
            agentId = note.getAgent().getId();
            String fn = note.getAgent().getFirstName() != null ? note.getAgent().getFirstName() : "";
            String ln = note.getAgent().getLastName() != null ? note.getAgent().getLastName() : "";
            agentNom = (fn + " " + ln).trim();
        }

        Long clientId = null;
        String clientNom = null;
        if (note.getClient() != null) {
            clientId = note.getClient().getId();
            String fn = note.getClient().getFirstName() != null ? note.getClient().getFirstName() : "";
            String ln = note.getClient().getLastName() != null ? note.getClient().getLastName() : "";
            clientNom = (fn + " " + ln).trim();
        }

        Long vehiculeId = null;
        String immat = null;
        String marque = null;
        String modele = null;
        String numeroChassis = null;
        if (note.getVehicule() != null) {
            vehiculeId = note.getVehicule().getId();
            immat = note.getVehicule().getImmatriculation();
            marque = note.getVehicule().getMarque();
            modele = note.getVehicule().getModele();
            numeroChassis = note.getVehicule().getNumeroChassis();
        }

        Long ordreId = null;
        String ordreNum = null;
        if (note.getOrdreReparation() != null) {
            ordreId = note.getOrdreReparation().getId();
            ordreNum = note.getOrdreReparation().getNumero();
        }

        return NoteDePrixListResponse.builder()
                .id(note.getId())
                .numero(note.getNumero())
                .dateCreation(note.getDateCreation())
                .montantHT(note.getMontantHT())
                .montantTotal(note.getMontantTotal())
                .montantPaye(note.getMontantPaye())
                .resteAPayer(note.getResteAPayer())
                .statutPaiement(note.getStatutPaiement() != null ? note.getStatutPaiement().name() : null)
                .modePaiement(note.getModePaiement())
                // .numeroBonDeCommande(note.getNumeroBonDeCommande())
                // .montantAutre(note.getMontantAutre())
                // .agentId(agentId)
                .agentNom(agentNom)
                // .clientId(clientId)
                .clientNom(clientNom)
                // .vehiculeId(vehiculeId)
                .vehiculeImmatriculation(immat)
                .immatriculation(immat)
                .marque(marque)
                .modele(modele)
                // .numeroChassis(numeroChassis)
                .kilometrage(note.getKilometrage())
                .remarque(note.getRemarque())
                // .statut(note.getStatut() != null ? note.getStatut().name() : null)
                // .ordreReparationId(ordreId)
                // .numeroOrdreReparation(ordreNum)
                .build();
    }
}
