package sn.oas.facturation.features.ficheAtelier.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevisPrevisionnelRequestFromFicheAtelierDto {

    @NotNull(message = "Le montant total est obligatoire")
    @Positive(message = "Le montant doit être supérieur à zéro")
    private Double montantTotal;

    private String notesReparation;        // Ex: "Changement plaquettes et révision"

    private Integer kilometrageVehicule;

    private Long clientId;

    private Long vehiculeId;

    private Long ficheAtelierId;           // Lié à la fiche atelier
}
