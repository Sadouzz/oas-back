package sn.oas.facturation.partenaire.dto;

import lombok.Data;
import sn.oas.facturation.partenaire.data.entity.TypePartenaire;
import java.time.LocalDateTime;

@Data
public class PartenaireResponse {
    private Long id;
    private String nom;
    private String description;
    private String logo;
    private TypePartenaire type;
    private boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
