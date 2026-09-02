package sn.oas.facturation.features.main_doeuvre.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.shared.entity.BaseEntity;

@Entity
@Table(name = "main_doeuvre")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainDoeuvre extends BaseEntity {

    @Column(nullable = false)
    private Double prix;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")
    private CategorieMainDoeuvre categorie;

    @Column(name = "nbre_heure", nullable = false)
    private Integer nbreHeure;

    @Column(name = "is_archived", nullable = false)
    @Builder.Default
    private Boolean isArchived = false;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_de_sortie_id", nullable = true)
    private BonDeSortie bonDeSortie;

    @PrePersist
    protected void onPrePersist() {
        if (this.isArchived == null) {
            this.isArchived = false;
        }
    }
}