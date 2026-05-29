package sn.oas.facturation.main_doeuvre.data.entity;

import jakarta.persistence.*;
import lombok.*;
import sn.oas.facturation.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.main_doeuvre.data.enums.CategorieMainDoeuvre;

@Entity
@Table(name = "main_doeuvre")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainDoeuvre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double prix;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorieMainDoeuvre categorie;

    @Column(name = "nbre_heure", nullable = false)
    private Integer nbreHeure;

    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_de_sortie_id", nullable = false)
    private BonDeSortie bonDeSortie;

    @PrePersist
    protected void onCreate() {
        if (this.isArchived == null) {
            this.isArchived = false;
        }
    }

}