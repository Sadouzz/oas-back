package sn.oas.facturation.features.piecedetache.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.garage.data.entity.Garage;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_garage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockGarage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id", nullable = false)
    private Garage garage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piece_id", nullable = false)
    private PieceDetache piece;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantite = 0;

    @Column
    private String emplacement;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}
