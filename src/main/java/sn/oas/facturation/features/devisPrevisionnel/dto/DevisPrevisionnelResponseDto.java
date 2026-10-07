package sn.oas.facturation.features.devisPrevisionnel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.devisPrevisionnel.data.entity.DevisPrevisionnel;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevisPrevisionnelResponseDto {

    private Long id;
    private String numero;                 // Ex: "DEV-2026-0031"
    private Double montantTotal;           // Ex: 150000.0
    private String statut;                 // "EN_ATTENTE", "ACCEPTE", "REJETE"
    private String notesReparation;        // Remarques saisies
    private Integer kilometrageVehicule;
    private LocalDateTime dateCreation;

    // Références légères (sans objets Hibernate complexes)
    private Long ficheAtelierId;
    private Long clientId;
    private String clientNom;
    private Long vehiculeId;
    private String vehiculeImmatriculation;

    public static DevisPrevisionnelResponseDto from(DevisPrevisionnel devis) {
        if (devis == null) {
            return null;
        }

        Long ficheAtelierId = devis.getFicheAtelier() != null ? devis.getFicheAtelier().getId() : null;
        Long clientId = null;
        String clientNom = null;
        if (devis.getClient() != null) {
            clientId = devis.getClient().getId();
            clientNom = ((devis.getClient().getFirstName() != null ? devis.getClient().getFirstName() : "") + " " +
                    (devis.getClient().getLastName() != null ? devis.getClient().getLastName() : "")).trim();
        }

        Long vehiculeId = null;
        String vehiculeImmatriculation = null;
        if (devis.getVehicule() != null) {
            vehiculeId = devis.getVehicule().getId();
            vehiculeImmatriculation = devis.getVehicule().getImmatriculation();
        }

        Double montant = devis.getMontantTotal() != null ? devis.getMontantTotal().doubleValue() : 0.0;
        Integer km = devis.getKilometrageVehicule() != null ? devis.getKilometrageVehicule().intValue() : null;
        String statutStr = devis.getStatut() != null ? devis.getStatut().name() : null;

        return DevisPrevisionnelResponseDto.builder()
                .id(devis.getId())
                .numero(devis.getNumero())
                .montantTotal(montant)
                .statut(statutStr)
                .notesReparation(devis.getNotesReparation())
                .kilometrageVehicule(km)
                .dateCreation(devis.getDateCreation())
                .ficheAtelierId(ficheAtelierId)
                .clientId(clientId)
                .clientNom(clientNom)
                .vehiculeId(vehiculeId)
                .vehiculeImmatriculation(vehiculeImmatriculation)
                .build();
    }
}
