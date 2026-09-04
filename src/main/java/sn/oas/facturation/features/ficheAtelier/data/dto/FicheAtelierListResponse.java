package sn.oas.facturation.features.ficheAtelier.data.dto;

import lombok.Builder;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier;

import java.time.LocalDateTime;

@Builder
public record FicheAtelierListResponse(
        Long id,
        // Long rendezVousId,
        // Long clientId,
        String clientName,
        // Long vehiculeId,
        String vehiculeImmatriculation,
        String nomChauffeur,
        // String telephoneChauffeur,
        // Integer kilometrage,
        // String designationTravaux,
        // LocalDateTime dateSortiePrevue,
        LocalDateTime createdAt,
        boolean hasOrdreReparation) {
    public static FicheAtelierListResponse from(FicheAtelier f) {
        return from(f, false);
    }

    public static FicheAtelierListResponse from(FicheAtelier f, boolean hasOrdreReparation) {
        if (f == null)
            return null;

        // Long clientId = null;
        String clientName = null;
        if (f.getClient() != null) {
            // clientId = f.getClient().getId();
            String fn = f.getClient().getFirstName() != null ? f.getClient().getFirstName() : "";
            String ln = f.getClient().getLastName() != null ? f.getClient().getLastName() : "";
            clientName = (fn + " " + ln).trim();
        }

        return FicheAtelierListResponse.builder()
                .id(f.getId())
                // .rendezVousId(f.getRendezVous() != null ? f.getRendezVous().getId() : null)
                // .clientId(clientId)
                .clientName(clientName)
                // .vehiculeId(f.getVehicule() != null ? f.getVehicule().getId() : null)
                .vehiculeImmatriculation(f.getVehicule() != null ? f.getVehicule().getImmatriculation() : null)
                .nomChauffeur(f.getNomChauffeur())
                // .telephoneChauffeur(f.getTelephoneChauffeur())
                // .kilometrage(f.getKilometrage())
                // .designationTravaux(f.getDesignationTravaux())
                // .dateSortiePrevue(f.getDateSortiePrevue())
                .createdAt(f.getCreatedAt())
                .hasOrdreReparation(hasOrdreReparation)
                .build();
    }
}
