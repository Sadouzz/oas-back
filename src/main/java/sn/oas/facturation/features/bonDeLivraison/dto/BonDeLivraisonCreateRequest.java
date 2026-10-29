package sn.oas.facturation.features.bonDeLivraison.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BonDeLivraisonCreateRequest {
    @NotNull private Long ordreReparationId;
    private Boolean appliquerTVA;
    private String remarque;
}
