package sn.oas.facturation.features.recu.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Reçu d'un client (table recu de l'ancienne application).
 * <p>
 * Un client a au plus un reçu en cours ({@code dateFin} nulle). Ce reçu couvre toutes ses factures et notes
 * de prix impayées et reçoit ses versements. Il est clôturé ({@code dateFin} renseignée) quand tout est payé,
 * puis peut être validé. Les documents payés d'un reçu sont ceux de ses versements.
 */
@Entity
@Table(name = "recus")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
public class Recu implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numéro du reçu : PREFIXE_GARAGE-RE-ANNEE-0001. */
    @Column(unique = true, length = 40)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id")
    private Garage garage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "date_debut", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime dateDebut;

    /** Date de paiement complet ; nulle tant que le reçu est en cours. */
    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    /** Total du reçu : montant déjà versé + reste à payer des documents du client. */
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal montant = BigDecimal.ZERO;

    @Column(name = "montant_paye", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal montantPaye = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private boolean valide = false;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @OneToMany(mappedBy = "recu")
    @OrderBy("dateVersement DESC, id DESC")
    @Builder.Default
    @lombok.EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private List<VersementClient> versements = new ArrayList<>();

    public boolean isEnCours() {
        return dateFin == null;
    }
}
