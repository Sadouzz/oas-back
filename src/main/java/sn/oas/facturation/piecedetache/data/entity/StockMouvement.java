package sn.oas.facturation.piecedetache.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.piecedetache.data.enums.TypeMouvement;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_mouvements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMouvement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMouvement type;

    @Column(nullable = false)
    private Integer quantite;

    @Column(name = "stock_magasin_avant", nullable = false)
    private Integer stockMagasinAvant;

    @Column(name = "stock_atelier_avant", nullable = false)
    private Integer stockAtelierAvant;

    @Column(name = "stock_magasin_apres", nullable = false)
    private Integer stockMagasinApres;

    @Column(name = "stock_atelier_apres", nullable = false)
    private Integer stockAtelierApres;

    private String motif;

    @Column(name = "date_operation", nullable = false, updatable = false)
    private LocalDateTime dateOperation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piece_id", nullable = false)
    private PDP piece;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @PrePersist
    protected void onCreate() {
        if (this.dateOperation == null) {
            this.dateOperation = LocalDateTime.now();
        }
    }
}
