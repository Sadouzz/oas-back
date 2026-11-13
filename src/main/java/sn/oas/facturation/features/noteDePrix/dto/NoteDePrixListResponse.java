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
        BigDecimal montantTVA,
        Boolean tvaApplicable,
        BigDecimal montantTotal,
        BigDecimal montantPaye,
        BigDecimal montantTimbrePaye,
        BigDecimal montantEncaisse,
        BigDecimal resteAPayer,
        String statutPaiement,
        String modePaiement,
        String numeroBonDeCommande,
        BigDecimal montantAutre,
        Long agentId,
        String agentNom,
        Long clientId,
        String clientNom,
        String clientMatricule,
        String clientNumero,
        String numeroClient,
        Long vehiculeId,
        String vehiculeImmatriculation,
        String immatriculation,
        String marque,
        String modele,
        String numeroChassis,
        Double kilometrage,
        String remarque,
        String statut,
        Long ordreReparationId,
        String numeroOrdreReparation
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

        var client = note.getClient();
        if (client == null && note.getOrdreReparation() != null) {
            client = note.getOrdreReparation().getClient() != null
                    ? note.getOrdreReparation().getClient()
                    : (note.getOrdreReparation().getVehicule() != null ? note.getOrdreReparation().getVehicule().getClient() : null);
        }
        if (client == null && note.getVehicule() != null) {
            client = note.getVehicule().getClient();
        }

        Long clientId = null;
        String clientNom = null;
        String clientMatricule = null;
        if (client != null) {
            clientId = client.getId();
            String fn = client.getFirstName() != null ? client.getFirstName() : "";
            String ln = client.getLastName() != null ? client.getLastName() : "";
            clientNom = (fn + " " + ln).trim();
            clientMatricule = client.getMatricule();
        }

        var vehicule = note.getVehicule();
        if (vehicule == null && note.getOrdreReparation() != null) {
            vehicule = note.getOrdreReparation().getVehicule();
        }

        Long vehiculeId = null;
        String immat = null;
        String marque = null;
        String modele = null;
        String numeroChassis = null;
        if (vehicule != null) {
            vehiculeId = vehicule.getId();
            immat = vehicule.getImmatriculation();
            marque = vehicule.getMarque();
            modele = vehicule.getModele();
            numeroChassis = vehicule.getNumeroChassis();
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
                .montantTVA(note.getMontantTVA())
                .tvaApplicable(note.getTvaApplicable())
                .montantTotal(note.getMontantTotal())
                .montantPaye(note.getMontantPaye())
                .montantTimbrePaye(note.getMontantTimbrePaye() != null ? note.getMontantTimbrePaye() : BigDecimal.ZERO)
                .montantEncaisse((note.getMontantPaye() != null ? note.getMontantPaye() : BigDecimal.ZERO)
                        .add(note.getMontantTimbrePaye() != null ? note.getMontantTimbrePaye() : BigDecimal.ZERO))
                .resteAPayer(note.getResteAPayer())
                .statutPaiement(note.getStatutPaiement() != null ? note.getStatutPaiement().name() : null)
                .modePaiement(note.getModePaiement())
                .numeroBonDeCommande(note.getNumeroBonDeCommande())
                .montantAutre(note.getMontantAutre())
                .agentId(agentId)
                .agentNom(agentNom)
                .clientId(clientId)
                .clientNom(clientNom)
                .clientMatricule(clientMatricule)
                .clientNumero(clientMatricule)
                .numeroClient(clientMatricule)
                .vehiculeId(vehiculeId)
                .vehiculeImmatriculation(immat)
                .immatriculation(immat)
                .marque(marque)
                .modele(modele)
                .numeroChassis(numeroChassis)
                .kilometrage(note.getKilometrage())
                .remarque(note.getRemarque())
                .statut(note.getStatut() != null ? note.getStatut().name() : null)
                .ordreReparationId(ordreId)
                .numeroOrdreReparation(ordreNum)
                .build();
    }
}
