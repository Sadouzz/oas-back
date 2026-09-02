package sn.oas.facturation.features.facturation.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;

@Entity
@Table(name = "lignes_facturation_piece")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneFacturationPiece {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facturation_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("lignesFacturationPieces")
    private Facturation facturation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piece_id", nullable = true) // <-- Nullable for custom pieces
    private PDP piece;

    @Builder.Default
    private Boolean isCustom = false;

    private String designationPds;

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
