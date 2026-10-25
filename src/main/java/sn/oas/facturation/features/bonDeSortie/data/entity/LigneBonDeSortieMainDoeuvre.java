package sn.oas.facturation.features.bonDeSortie.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.main_doeuvre.data.entity.MainDoeuvre;

import java.math.BigDecimal;

@Entity
@Table(name = "lignes_bon_de_sortie_main_doeuvre")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LigneBonDeSortieMainDoeuvre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_de_sortie_id", nullable = false)
    private BonDeSortie bonDeSortie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_doeuvre_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MainDoeuvre mainDoeuvre;

    @Column(name = "description_snapshot")
    private String description;

    @Builder.Default
    @Column(name = "nbre_heure", nullable = false)
    private Integer nbreHeure = 1;

    @Builder.Default
    @Column(name = "tarif_horaire", nullable = false)
    private Integer tarifHoraire = 0;

    @Builder.Default
    @Column(name = "taux_remise", precision = 5, scale = 2)
    private BigDecimal tauxRemise = BigDecimal.ZERO;

    @Column(name = "montant_total", precision = 15, scale = 2)
    private BigDecimal montantTotal;

    @PrePersist
    @PreUpdate
    protected void onPersist() {
        if (this.nbreHeure == null || this.nbreHeure <= 0) {
            this.nbreHeure = 1;
        }
        if (this.tarifHoraire == null) {
            this.tarifHoraire = 0;
        }
        if (this.tauxRemise == null) {
            this.tauxRemise = BigDecimal.ZERO;
        }
        BigDecimal base = BigDecimal.valueOf(this.tarifHoraire).multiply(BigDecimal.valueOf(this.nbreHeure));
        if (this.tauxRemise.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal remise = base.multiply(this.tauxRemise).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            this.montantTotal = base.subtract(remise);
        } else {
            this.montantTotal = base;
        }
    }
}
