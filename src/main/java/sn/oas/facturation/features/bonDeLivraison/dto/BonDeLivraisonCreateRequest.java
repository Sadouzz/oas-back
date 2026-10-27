package sn.oas.facturation.features.bonDeLivraison.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class BonDeLivraisonCreateRequest {
    @NotNull private Long ordreReparationId;
    @NotEmpty private List<@NotNull Long> bonsDeSortieIds;
    private Boolean appliquerTVA;
    private String remarque;
}
