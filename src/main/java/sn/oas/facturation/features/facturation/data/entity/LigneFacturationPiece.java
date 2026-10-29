package sn.oas.facturation.features.facturation.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private PieceDetache piece;

    @Builder.Default
    private Boolean isCustom = false;

    private String designationPds;

    @Column(nullable = false)
    private Integer quantite;
    
    @Column(nullable = false)
    private Integer prix;

    @Builder.Default
    @Column(name = "taux_remise", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxRemise = BigDecimal.ZERO;

    @ElementCollection
    @CollectionTable(name = "ligne_facturation_piece_dates_source", joinColumns = @JoinColumn(name = "ligne_facturation_piece_id"))
    @OrderColumn(name = "position")
    @Column(name = "date_heure_saisie", nullable = false)
    @Builder.Default
    private List<LocalDateTime> datesHeuresSaisieSource = new ArrayList<>();

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
