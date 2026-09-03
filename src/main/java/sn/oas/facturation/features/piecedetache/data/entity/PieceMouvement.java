package sn.oas.facturation.features.piecedetache.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.piecedetache.data.enums.TypeMouvement;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDateTime;

@Entity
@Table(name = "piece_mouvements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
public class PieceMouvement implements TenantAware {

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

    @Column(name = "stock_reel_apres")
    private Double stockReelApres;

    private String prenom;

    private String nom;

    @Column(name = "num_document")
    private String numDocument;

    @Column(name = "type_document")
    private String typeDocument;

    @Column(name = "numero_serie")
    private String numeroSerie;

    private String immatriculation;

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
