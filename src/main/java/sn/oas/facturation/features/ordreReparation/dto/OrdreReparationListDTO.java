package sn.oas.facturation.features.ordreReparation.dto;

import lombok.Builder;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;

import java.time.LocalDateTime;

@Builder
public record OrdreReparationListDTO(
        Long id,
        String numero,
        String descriptionTravaux,
        LocalDateTime dateCreation,
        LocalDateTime dateSortie,
        StatutOrdreReparation statut,
        VehiculeSummary vehicule,
        // ClientSummary client,
        // String clientName,
        // Long ficheAtelierId,
        Boolean hasPiecesOrMo
) {
    public record VehiculeSummary(
            Long id,
            String immatriculation,
            String marque,
            String modele,
            ClientSummary client
    ) {}

    public record ClientSummary(
            Long id,
            String firstName,
            String lastName
            // String phone
    ) {}

    public static OrdreReparationListDTO from(OrdreReparation o) {
        if (o == null) return null;

        ClientSummary clientSummary = null;
        // String clientName = null;
        if (o.getVehicule() != null && o.getVehicule().getClient() != null) {
            var c = o.getVehicule().getClient();
            clientSummary = new ClientSummary(
                    c.getId(),
                    c.getFirstName(),
                    c.getLastName()
                    // c.getPhone()
            );
            // String fn = c.getFirstName() != null ? c.getFirstName() : "";
            // String ln = c.getLastName() != null ? c.getLastName() : "";
            // clientName = (fn + " " + ln).trim();
        }

        VehiculeSummary vehiculeSummary = null;
        if (o.getVehicule() != null) {
            vehiculeSummary = new VehiculeSummary(
                    o.getVehicule().getId(),
                    o.getVehicule().getImmatriculation(),
                    o.getVehicule().getMarque(),
                    o.getVehicule().getModele(),
                    clientSummary
            );
        }

        return OrdreReparationListDTO.builder()
                .id(o.getId())
                .numero(o.getNumero())
                .descriptionTravaux(o.getDescriptionTravaux())
                .dateCreation(o.getDateCreation())
                .dateSortie(o.getDateSortie())
                .statut(o.getStatut())
                .vehicule(vehiculeSummary)
                // .client(clientSummary)
                // .clientName(clientName)
                // .ficheAtelierId(o.getFicheAtelier() != null ? o.getFicheAtelier().getId() : null)
                .hasPiecesOrMo(o.getHasPiecesOrMo())
                .build();
    }
}
