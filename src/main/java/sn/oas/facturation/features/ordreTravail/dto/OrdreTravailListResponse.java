package sn.oas.facturation.features.ordreTravail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.ordreTravail.data.entity.OrdreTravail;
import sn.oas.facturation.features.ordreTravail.data.enums.StatutOrdreTravail;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdreTravailListResponse {
    private Long id;
    private String numero;
    private String clientNom;
    private String vehiculeImmatriculation;
    private String vehiculeMarqueModele;
    private String ordreReparationNumero;
    private StatutOrdreTravail statut;
    private String statutLabel;
    private Integer etape;
    private int nombreTechniciens;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrdreTravailListResponse from(OrdreTravail ot) {
        if (ot == null) return null;

        String clientNom = null;
        if (ot.getClient() != null) {
            clientNom = ((ot.getClient().getFirstName() != null ? ot.getClient().getFirstName() + " " : "")
                    + (ot.getClient().getLastName() != null ? ot.getClient().getLastName() : "")).trim();
        }

        String immat = ot.getVehicule() != null ? ot.getVehicule().getImmatriculation() : null;
        String marqueModele = null;
        if (ot.getVehicule() != null) {
            marqueModele = ((ot.getVehicule().getMarque() != null ? ot.getVehicule().getMarque() + " " : "")
                    + (ot.getVehicule().getModele() != null ? ot.getVehicule().getModele() : "")).trim();
        }

        return OrdreTravailListResponse.builder()
                .id(ot.getId())
                .numero(ot.getNumero())
                .clientNom(clientNom)
                .vehiculeImmatriculation(immat)
                .vehiculeMarqueModele(marqueModele)
                .ordreReparationNumero(ot.getOrdreReparation() != null ? ot.getOrdreReparation().getNumero() : null)
                .statut(ot.getStatut())
                .statutLabel(ot.getStatut() != null ? ot.getStatut().getLabel() : null)
                .etape(ot.getStatut() != null ? ot.getStatut().getEtape() : null)
                .nombreTechniciens(ot.getLignesTechniciens() != null ? ot.getLignesTechniciens().size() : 0)
                .createdAt(ot.getCreatedAt())
                .updatedAt(ot.getUpdatedAt() != null ? ot.getUpdatedAt() : ot.getCreatedAt())
                .build();
    }
}
