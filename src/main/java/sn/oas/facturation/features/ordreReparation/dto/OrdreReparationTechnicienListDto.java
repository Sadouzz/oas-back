package sn.oas.facturation.features.ordreReparation.dto;

import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;

import java.time.LocalDateTime;

public record OrdreReparationTechnicienListDto(
    Long id,
    String numero,
    String statut,
    LocalDateTime dateCreation,
    VehiculeSummaryDto vehicule
) {
    public record VehiculeSummaryDto(
        Long id,
        String immatriculation,
        String marque,
        String modele
    ) {}

    public static OrdreReparationTechnicienListDto fromEntity(OrdreReparation or) {
        if (or == null) {
            return null;
        }
        VehiculeSummaryDto vehiculeDto = null;
        if (or.getVehicule() != null) {
            vehiculeDto = new VehiculeSummaryDto(
                or.getVehicule().getId(),
                or.getVehicule().getImmatriculation(),
                or.getVehicule().getMarque(),
                or.getVehicule().getModele()
            );
        }
        return new OrdreReparationTechnicienListDto(
            or.getId(),
            or.getNumero(),
            or.getStatut() != null ? or.getStatut().name() : null,
            or.getDateCreation(),
            vehiculeDto
        );
    }
}
