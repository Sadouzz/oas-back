package sn.oas.facturation.piecedetache.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.piecedetache.data.enums.TypeMouvement;


import sn.oas.facturation.garage.data.entity.Garage;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_mouvements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
public class StockMouvement implements TenantAware {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id")
    private Garage garage;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMouvement type;

    @Column(nullable = false)
    private Double quantite;

    @Column(name = "stock_magasin_avant", nullable = false)
    private Double stockMagasinAvant;

    @Column(name = "stock_atelier_avant", nullable = false)
    private Double stockAtelierAvant;

    @Column(name = "stock_magasin_apres", nullable = false)
    private Double stockMagasinApres;

    @Column(name = "stock_atelier_apres", nullable = false)
    private Double stockAtelierApres;

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
