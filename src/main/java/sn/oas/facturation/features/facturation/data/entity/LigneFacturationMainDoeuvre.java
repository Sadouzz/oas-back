package sn.oas.facturation.features.facturation.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.main_doeuvre.data.entity.MainDoeuvre;

@Entity
@Table(name = "lignes_facturation_main_doeuvre")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneFacturationMainDoeuvre {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facturation_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("lignesFacturationMainDoeuvres")
    private Facturation facturation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_doeuvre_id", nullable = false)
    private MainDoeuvre mainDoeuvre;

    @Column(nullable = false)
    private Integer nbreHeure;
    
    @Column(nullable = false)
    private Integer tarifHoraire;

    @PrePersist
    protected void onCreate() {
        if (this.nbreHeure == null) {
            this.nbreHeure = 1;
        }
        if (this.tarifHoraire == null) {
            this.tarifHoraire = 0;
        }
    }
}
