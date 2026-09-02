package sn.oas.facturation.features.partenaire.data.entity;

import jakarta.persistence.*;
import lombok.*;
import sn.oas.facturation.shared.entity.BaseEntity;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partenaire extends BaseEntity {

    @Column(nullable = false)
    private String nom;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String logo;
    
    @Enumerated(EnumType.STRING)
    private TypePartenaire type;
    
    @Builder.Default
    private boolean archived = false;
}
