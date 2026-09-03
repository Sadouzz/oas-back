package sn.oas.facturation.features.partenaire.dto;

import lombok.Data;
import sn.oas.facturation.features.partenaire.data.enums.TypePartenaire;

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

    public static PartenaireResponse from(sn.oas.facturation.features.partenaire.data.entity.Partenaire partenaire) {
        if (partenaire == null) return null;
        PartenaireResponse response = new PartenaireResponse();
        org.springframework.beans.BeanUtils.copyProperties(partenaire, response);
        return response;
    }
}
