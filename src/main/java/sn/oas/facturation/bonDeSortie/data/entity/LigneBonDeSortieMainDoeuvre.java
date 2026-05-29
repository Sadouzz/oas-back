package sn.oas.facturation.bonDeSortie.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.oas.facturation.main_doeuvre.data.entity.MainDoeuvre;

@Entity
@Table(name = "lignes_bon_de_sortie_main_doeuvre")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LigneBonDeSortieMainDoeuvre {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_de_sortie_id", nullable = false)
    private BonDeSortie bonDeSortie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_doeuvre_id", nullable = false)
    private MainDoeuvre mainDoeuvre;

    @Column(nullable = false)
    private Integer quantite;
    
    @Column(nullable = false)
    private Integer prix;

    @PrePersist
    protected void onCreate() {
        if (this.quantite == null) {
            this.quantite = 1;
        }
        if (this.prix == null) {
            this.prix = 0;
        }
    }
}
