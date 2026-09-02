package sn.oas.facturation.features.partenaire.dto;

import lombok.Data;
import sn.oas.facturation.features.partenaire.data.entity.TypePartenaire;

@Data
public class PartenaireRequest {
    private String nom;
    private String description;
    private String logo;
    private TypePartenaire type;
    private boolean archived;
}
