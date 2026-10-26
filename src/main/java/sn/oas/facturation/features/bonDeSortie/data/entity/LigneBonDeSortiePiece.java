package sn.oas.facturation.features.bonDeSortie.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.features.bonDeSortie.data.enums.TypeLigneBonDeSortie;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@Entity
@Table(name = "lignes_bon_de_sortie_piece")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LigneBonDeSortiePiece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_de_sortie_id", nullable = false)
    private BonDeSortie bonDeSortie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piece_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PieceDetache piece;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_ligne", nullable = false)
    @Builder.Default
    private TypeLigneBonDeSortie type = TypeLigneBonDeSortie.PIECE;

    /** Identifiant conservé pour lire les anciennes lignes de main-d'œuvre stockées dans cette table. */
    @Column(name = "main_doeuvre_id")
    private Long mainDoeuvreId;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "main_doeuvre_id")
    // private MainDoeuvre mainDoeuvre;

    @Column(name = "designation_snapshot")
    private String designation;

    @Column(nullable = false)
    private Integer quantite;
    
    @Column(nullable = false)
    private Integer prix;

    @Builder.Default
    @Column(name = "taux_remise", precision = 5, scale = 2)
    private BigDecimal tauxRemise = BigDecimal.ZERO;

    @Column(name = "montant_total", precision = 15, scale = 2)
    private BigDecimal montantTotal;

    public BigDecimal getTauxRemise() {
        return tauxRemise != null ? tauxRemise : BigDecimal.ZERO;
    }

    @PrePersist
    @PreUpdate
    protected void onPersist() {
        if (this.quantite == null || this.quantite <= 0) {
            this.quantite = 1;
        }
        if (this.prix == null) {
            this.prix = 0;
        }
        if (this.tauxRemise == null) {
            this.tauxRemise = BigDecimal.ZERO;
        }
        BigDecimal base = BigDecimal.valueOf(this.prix).multiply(BigDecimal.valueOf(this.quantite));
        if (this.tauxRemise.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal remise = base.multiply(this.tauxRemise).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            this.montantTotal = base.subtract(remise);
        } else {
            this.montantTotal = base;
        }
    }
}
