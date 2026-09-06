package sn.oas.facturation.features.devisPrevisionnel.dto;

import lombok.Builder;
import sn.oas.facturation.features.devisPrevisionnel.data.entity.DevisPrevisionnel;
import sn.oas.facturation.features.facturation.data.enums.StatutFacturation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record DevisPrevisionnelListResponse(
        Long id,
        String numero,
        String notesReparation,
        BigDecimal montantTotal,
        Double kilometrageVehicule,
        // LocalDateTime dateCreation,
        // LocalDateTime createdAt,
        StatutFacturation statut,
        VehiculeSummary vehicule,
        ClientSummary client
        // Long ficheAtelierId
) {
    public record VehiculeSummary(
            Long id,
            String immatriculation,
            String marque
            // String modele
    ) {}
    public record ClientSummary(
            Long id,
            String firstName,
            String lastName
            // String phone
    ) {}

    public static DevisPrevisionnelListResponse from(DevisPrevisionnel d) {
        if (d == null) return null;

        VehiculeSummary vehiculeSummary = null;
        if (d.getVehicule() != null) {
            vehiculeSummary = new VehiculeSummary(
                    d.getVehicule().getId(),
                    d.getVehicule().getImmatriculation(),
                    d.getVehicule().getMarque()
                    // d.getVehicule().getModele()
            );
        }

        ClientSummary clientSummary = null;
        if (d.getClient() != null) {
            clientSummary = new ClientSummary(
                    d.getClient().getId(),
                    d.getClient().getFirstName(),
                    d.getClient().getLastName()
                    // d.getClient().getPhone()
            );
        }

        return DevisPrevisionnelListResponse.builder()
                .id(d.getId())
                .numero(d.getNumero())
                .notesReparation(d.getNotesReparation())
                .montantTotal(d.getMontantTotal())
                .kilometrageVehicule(d.getKilometrageVehicule())
                // .dateCreation(d.getDateCreation())
                // .createdAt(d.getDateCreation())
                .statut(d.getStatut())
                .vehicule(vehiculeSummary)
                .client(clientSummary)
                // .ficheAtelierId(d.getFicheAtelier() != null ? d.getFicheAtelier().getId() : null)
                .build();
    }
}
